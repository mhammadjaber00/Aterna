# Aterna — Story System README (Chapter 1 Included)

A complete, maintainable, deterministic narrative layer for Aterna — a focus app with an RPG skin. This README contains:

- Design goals & architecture
- Determinism contract
- Database schema (SQLDelight-style)
- Runtime flow & integration points
- Chapter 1 content pack (objectives, story events)
- Full beat catalogs (openers/closers/retreat/ambient/chest/mob/quirky/trinket/system)
- Event→Thread mapping spec
- Notification & UI microcopy
- Analytics event names
- QA/validation prompts & test plans
- Seeding instructions & migration notes

> **Scope guard:** Chapter 1 uses low‑tier threats and village/forest flavor. No dragons, no high magic. The hero is
> weak and learning.

---

## 1) North Star

- **Core:** a focus timer with deterministic rewards (ledger is truth).
- **Skin:** living micro‑stories that sit *next to* the economy, never inside it.
- **Spine:** a data‑driven narrative engine that’s seeded and testable.

**Design tenets**

- Short lines. Wry tone. Mobile friendly.
- No per‑second DB writes. Emit only on due events/ambients.
- Deterministic per session; rebuildable after process death.
- Chapter/objectives are data. Content is versioned packs.

---

## 2) Glossary & Placeholders

Use these placeholders in content. Keep casing exact:

- `$HERO_NAME`, `$CLASS` (WARRIOR/MAGE/ROGUE, etc.)
- `$PRONOUN_SUBJ` (he/she/they), `$PRONOUN_OBJ`, `$PRONOUN_POSS`
- `$XP`, `$GOLD`, `$ITEM`, `$ITEM_COUNT`
- `$MOB_NAME`, `$MOB_LEVEL`, `$BIOME`, `$PLACE`
- `$OBJ_NAME`, `$OBJ_PROGRESS`, `$OBJ_TOTAL`
- `$CURSE_MINUTES`
- `$CHAPTER_ID`, `$CHAPTER_TITLE`

**Allowed mobs (Ch.1):** GOBLIN, WOLF, SKELETON, THIEF, SLIME.  
**Forbidden in Ch.1:** dragons; high wizards (use elder/scribe instead).

---

## 3) Architecture Overview

### Modules

- **:domain:story** (pure logic, no I/O)
    - `StoryDirector` — decides if/when to fire ambient or reskin a MOB.
    - `StoryEmitter` — converts a “moment” to a **thread with beats**.
    - `ObjectiveEngine` — updates chapter progress (kill/collect/visit).
    - `StoryMapper` — DB rows → UI view state (no business logic).
- **:data:story** (persistence & content)
    - `StoryRepository`, `ChapterRepository`, `ObjectiveRepository`.
    - Content packs (JSON/YAML) → DB seeds (versioned).
- **:features:quest** (integration)
    - Existing `QuestEventsCoordinator` stays owner of due events.
    - `StoryOrchestrator` (glue) subscribes to coordinator ticks, calls
      Director/Emitter/Objective, persists via repos, updates store.
- **:features:story-ui** (Compose surfaces)
    - Adventure Log (series of event threads), Chapter panel, Objective chip,
      Portal ticker. Reads `StoryViewState`.

### Determinism contract

```
storySeed = baseSeed
perEventSeed   = storySeed + (idx * 1_337L) + salt("EVENT")
perAmbientSeed = storySeed + hash("AMBIENT") + windowIndex
```

Same inputs → same world. Good for snapshot tests.

---

## 4) Database Schema (SQLDelight‑style)

> File names are suggestions; adapt to your project. Types are simple for portability.

```sql
-- chapters.sq
CREATE TABLE chapters
(
    id           TEXT    NOT NULL PRIMARY KEY,
    idx          INTEGER NOT NULL,
    title        TEXT    NOT NULL,
    blurb        TEXT    NOT NULL,
    unlock_level INTEGER NOT NULL
);

CREATE INDEX chapters_idx ON chapters (idx);
```

```sql
-- objectives.sq
CREATE TABLE objectives
(
    id                 TEXT    NOT NULL PRIMARY KEY,
    chapter_id         TEXT    NOT NULL REFERENCES chapters (id),
    type               TEXT    NOT NULL, -- KILL | COLLECT | VISIT
    target_key         TEXT    NOT NULL, -- e.g., mob:GOBLIN, item:WOLF_PELT, place:FOOTBRIDGE
    target_count       INTEGER NOT NULL,
    display_text       TEXT    NOT NULL,
    reward_item_id     TEXT,
    unlocks_chapter_id TEXT
);

CREATE INDEX objectives_chapter ON objectives (chapter_id);
```

```sql
-- objective_progress.sq
CREATE TABLE objective_progress
(
    hero_id      TEXT    NOT NULL,
    objective_id TEXT    NOT NULL REFERENCES objectives (id),
    count        INTEGER NOT NULL DEFAULT 0,
    completed_at INTEGER, -- epoch seconds
    PRIMARY KEY (hero_id, objective_id)
);
```

```sql
-- story_event_defs.sq
CREATE TABLE story_event_defs
(
    id                 TEXT NOT NULL PRIMARY KEY,
    key                TEXT NOT NULL UNIQUE,
    chapter_id         TEXT,          -- nullable if global
    kind               TEXT NOT NULL, -- AMBIENT | ENCOUNTER | CUTSCENE
    trigger_rules_json TEXT NOT NULL,
    beats_key          TEXT NOT NULL,
    payload_json       TEXT           -- e.g., {"drop_item_key":"GOBLIN_TAG","drop_chance":0.35}
);

CREATE INDEX story_event_defs_kind ON story_event_defs (kind);
```

```sql
-- story_event_occurrences.sq
CREATE TABLE story_event_occurrences
(
    id          TEXT    NOT NULL PRIMARY KEY,
    quest_id    TEXT    NOT NULL,
    def_id      TEXT    NOT NULL REFERENCES story_event_defs (id),
    at          INTEGER NOT NULL,
    outcome     TEXT    NOT NULL, -- NONE | SUCCESS | FLEE
    delta_count INTEGER NOT NULL DEFAULT 0,
    item_key    TEXT
);

CREATE INDEX seo_quest ON story_event_occurrences (quest_id);
CREATE INDEX seo_def ON story_event_occurrences (def_id);
```

```sql
-- story_threads.sq
CREATE TABLE story_threads
(
    id         TEXT    NOT NULL PRIMARY KEY,
    quest_id   TEXT    NOT NULL,
    source     TEXT    NOT NULL, -- PLAN_EVENT | STORY_EVENT | SYNTHETIC_START | SYN_END | SYN_RETREAT
    ref_idx    INTEGER,          -- plan idx if PLAN_EVENT
    title      TEXT    NOT NULL,
    order_idx  INTEGER NOT NULL,
    created_at INTEGER NOT NULL
);

CREATE INDEX story_threads_quest ON story_threads (quest_id);
CREATE INDEX story_threads_order ON story_threads (quest_id, order_idx);
```

```sql
-- story_beats.sq
CREATE TABLE story_beats
(
    thread_id TEXT    NOT NULL REFERENCES story_threads (id),
    idx       INTEGER NOT NULL,
    at        INTEGER NOT NULL,
    kind      TEXT    NOT NULL, -- Narration | Battle | Loot | Quirk | System
    text      TEXT    NOT NULL,
    tag       TEXT,
    PRIMARY KEY (thread_id, idx)
);

CREATE INDEX story_beats_thread ON story_beats (thread_id);
```

**Migration note:** bump schema version N→N+1; backfill none.

---

## 5) Runtime Flow

1) **Quest start**
    - `StoryDirector.beginSession` picks `$BIOME` (VILLAGE/FOREST) & mood.
    - Precomputes 3–5 ambient windows per run; scans major MOBs for reskin candidates.
    - `StoryEmitter.synthesizeStart` persists a START thread (class‑aware opener).

2) **During quest**
    - On each resolved **plan event**: keep ledger path.
        - Director may mark it for **reskin** (e.g., Goblin Lookout).
        - Emitter builds an **Event Thread** with 1–4 beats; persist.
        - ObjectiveEngine increments progress for KILL/COLLECT/ VISIT when relevant.
    - On **ambient window** tick: Director `maybeFireAmbient` → Emitter writes AMBIENT thread.

3) **End/Retreat**
    - Emitter writes FINISH or RETREAT synthetic thread.
    - On curse/bank, append System beats.

4) **UI**
    - Adventure Log shows a **series of Event Threads** (latest expanded).
    - Portal ticker and ongoing notification mirror the **latest beat**.
    - Chapter panel displays objective chips & bars.

---

## 6) Chapter 1 Content Pack

### 6.1 Chapter JSON (A1)

```json
{
  "chapter": {
    "id": "ch_1_errands_at_the_edge",
    "index": 1,
    "title": "Errands at the Edge",
    "blurb": "A village asks for small help. The forest asks for more.",
    "unlock_level": 1
  },
  "objectives": [
    {
      "id": "obj_goblin_tags",
      "type": "KILL",
      "target_key": "mob:GOBLIN",
      "target_count": 3,
      "display_text": "Collect goblin tags (wins count).",
      "reward_item_id": null,
      "unlocks_chapter_id": null
    },
    {
      "id": "obj_wolf_pelts",
      "type": "COLLECT",
      "target_key": "item:WOLF_PELT",
      "target_count": 5,
      "display_text": "Gather wolf pelts.",
      "reward_item_id": null,
      "unlocks_chapter_id": null
    },
    {
      "id": "obj_visit_footbridge",
      "type": "VISIT",
      "target_key": "place:FOOTBRIDGE",
      "target_count": 1,
      "display_text": "Check the old footbridge.",
      "reward_item_id": null,
      "unlocks_chapter_id": "ch_2_tbd"
    }
  ]
}
```

### 6.2 Ambient Story Event Definitions (B1)

```yaml
ambient_events:
  - id: "amb_village_cat"
    biome: "VILLAGE"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 20
      probability_per_10m: 0.25
      cooldown_minutes: 15
    beats_key: "VILLAGE_CAT"
  - id: "amb_village_bread"
    biome: "VILLAGE"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 20
      probability_per_10m: 0.20
      cooldown_minutes: 15
    beats_key: "VILLAGE_BREAD"
  - id: "amb_village_cart"
    biome: "VILLAGE"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 20
      probability_per_10m: 0.18
      cooldown_minutes: 20
    beats_key: "VILLAGE_CART_STUCK"
  - id: "amb_village_lanterns"
    biome: "VILLAGE"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.15
      cooldown_minutes: 25
    beats_key: "VILLAGE_LANTERNS"
  - id: "amb_village_thief_sneeze"
    biome: "VILLAGE"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 20
      probability_per_10m: 0.12
      cooldown_minutes: 30
    beats_key: "VILLAGE_THIEF_SNEEZE"

  - id: "amb_forest_deer"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.22
      cooldown_minutes: 15
    beats_key: "FOREST_DEER"
  - id: "amb_forest_wind"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.25
      cooldown_minutes: 10
    beats_key: "FOREST_WIND"
  - id: "amb_forest_crows"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.18
      cooldown_minutes: 20
    beats_key: "FOREST_CROWS"
  - id: "amb_forest_stream"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.20
      cooldown_minutes: 20
    beats_key: "FOREST_STREAM"
  - id: "amb_forest_pebble"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.14
      cooldown_minutes: 25
    beats_key: "FOREST_PEBBLE"
  - id: "amb_forest_mushroom"
    biome: "FOREST"
    kind: "AMBIENT"
    trigger:
      min_level: 1
      max_level: 25
      probability_per_10m: 0.12
      cooldown_minutes: 30
    beats_key: "FOREST_MUSHROOM"
```

### 6.3 Encounter Story Event Definitions (B2)

```yaml
encounter_events:
  - id: "enc_goblin_lookout"
    eligible_mobs: [ "GOBLIN" ]
    major_only: true
    min_level: 1
    max_level: 15
    probability_per_event: 0.15
    cooldown_minutes: 20
    drop_item_key: "GOBLIN_TAG"
    drop_chance: 0.35

  - id: "enc_wolf_pair"
    eligible_mobs: [ "WOLF" ]
    major_only: false
    min_level: 1
    max_level: 18
    probability_per_event: 0.18
    cooldown_minutes: 18
    drop_item_key: "WOLF_PELT"
    drop_chance: 0.40

  - id: "enc_thief_in_alley"
    eligible_mobs: [ "THIEF" ]
    major_only: false
    min_level: 1
    max_level: 20
    probability_per_event: 0.20
    cooldown_minutes: 25

  - id: "enc_graveyard_rattle"
    eligible_mobs: [ "SKELETON" ]
    major_only: true
    min_level: 5
    max_level: 25
    probability_per_event: 0.10
    cooldown_minutes: 30

  - id: "enc_slime_in_culvert"
    eligible_mobs: [ "SLIME" ]
    major_only: false
    min_level: 1
    max_level: 20
    probability_per_event: 0.22
    cooldown_minutes: 12

  - id: "enc_goblin_snare"
    eligible_mobs: [ "GOBLIN" ]
    major_only: false
    min_level: 1
    max_level: 16
    probability_per_event: 0.12
    cooldown_minutes: 16
    drop_item_key: "GOBLIN_TAG"
    drop_chance: 0.25
```

---

## 7) Beat Catalogs (FULL LINES)

Keep lines short; avoid dragons/wizard; use placeholders where noted.

### 7.1 START — Openers (C1)

```json
{
  "openers": {
    "common": [
      "$HERO_NAME checks the sky. New quest.",
      "Boots on. World off. Quest on.",
      "You breathe in. It tastes like trouble.",
      "A list in your head. A path underfoot.",
      "You tie a knot, tighter than needed.",
      "You speak a promise no one hears.",
      "You stretch the ache out of your hands.",
      "Maps? Later. Feet? Now.",
      "You touch the charm in your pocket.",
      "Quiet morning. Loud plans.",
      "You nod to nobody in particular.",
      "A sparrow heckles you. You go anyway.",
      "The gate creaks a blessing as you pass.",
      "You count steps until you stop counting.",
      "Fresh mud. Old habits.",
      "You choose forward. Again.",
      "You breathe out and begin.",
      "A thread of luck catches on you.",
      "You pocket a pencil and a hope.",
      "Sunlight snags on your sleeve.",
      "The road pretends it didn’t miss you.",
      "You shrug off the last excuse.",
      "You clock the wind. It’s with you.",
      "Another run. Keep it simple."
    ],
    "warrior": [
      "$HERO_NAME cracks knuckles. Time to work.",
      "Straps tight. Edges keen. Go.",
      "You shoulder the day like a shield.",
      "Steel’s quiet. That’s the good sign.",
      "You roll your neck. The world flinches.",
      "Old dents, new reasons.",
      "You trust your boots more than luck.",
      "Grip. Stance. Forward.",
      "You nod to the training yard and pass it.",
      "A breath. A step. A promise kept.",
      "You test your weight. Solid.",
      "You keep the blade sheathed. For now.",
      "Shoulder pops. Road answers.",
      "You walk like a door through fog.",
      "Rust scrapes off your courage.",
      "You count scars like prayers.",
      "Leather creaks. Day starts.",
      "You carry quiet like a weapon.",
      "You pick a fight with the distance.",
      "Knots and buckles. Then earth.",
      "You grip the hilt. Let go. Move.",
      "You stare the morning down first.",
      "You tuck strength into your stride.",
      "You go where trouble went."
    ],
    "mage": [
      "$HERO_NAME snaps a page shut. Field work.",
      "Ink dries. Curiosity doesn’t.",
      "You pocket chalk and a theory.",
      "You hum a pattern, then hush.",
      "You taste the air for answers.",
      "You fold a note into your sleeve.",
      "You count aloud. The day pretends not to listen.",
      "You trace a sigil only you can see.",
      "Your satchel argues. You win.",
      "You seal a jar of patience.",
      "You mark today: experiment.",
      "You press a pebble for luck.",
      "You list variables. You go anyway.",
      "You bait the unknown with a grin.",
      "Theory is warm. Roads are warmer.",
      "You set a ward on your nerves.",
      "You uncurl a question like a map.",
      "You shake crumbs off your notes.",
      "You whisper, then pretend you didn’t.",
      "You choose messy discovery.",
      "You keep one eye on the horizon.",
      "You pack extra chalk. Just in case.",
      "You measure nothing. You start.",
      "You tuck wonder behind your ear."
    ],
    "rogue": [
      "$HERO_NAME flips a coin. Lets it fall.",
      "You smirk at the easy route and miss it on purpose.",
      "Pockets light. Steps lighter.",
      "You wink at a locked door.",
      "You thread alleys like needles.",
      "You promise to behave. You don’t.",
      "You weigh the risk. Take it anyway.",
      "You tip your hood to the day.",
      "You catalog escapes, then go forward.",
      "You keep a lie ready for luck.",
      "You tuck a grin into your collar.",
      "Footfalls like quiet jokes.",
      "You greet the shadows like friends.",
      "Loose cobble, loose plans, tight smile.",
      "You pocket a borrowed plan.",
      "You practice innocence. Badly.",
      "You set a pace the world can’t audit.",
      "You steal a head start.",
      "You pat every pocket. Twice.",
      "You nod at a rooftop, keep street.",
      "You taste a shortcut and ignore it.",
      "You trade caution for rhythm.",
      "You let the day think it’s in charge.",
      "You go before nerves catch up."
    ]
  }
}
```

### 7.2 FINISH — Closers (C2)

```json
{
  "closers": [
    "Back to camp. Boots dusty.",
    "You call it. Good run.",
    "Firelight wins. You head home.",
    "You pocket the day and zip it.",
    "Enough for now. You’ll be back.",
    "You leave the path where it lies.",
    "You bow to the evening.",
    "You trade road for rest.",
    "Notes filed. Feet done.",
    "You hand the day a nod.",
    "You earned the quiet.",
    "You save a little luck for later.",
    "You hang up your thoughts.",
    "You bank the small win.",
    "Camp smells like relief.",
    "You pass the gate and grin.",
    "You set the pack down first.",
    "You call dibs on sleep.",
    "Good work. Lights down.",
    "You shelve the road for tomorrow.",
    "You thank your knees.",
    "You hush the hunger with plans.",
    "You melt into a chair.",
    "Gonna catch some zzz."
  ]
}
```

### 7.3 RETREAT — Quick / Late / Cursed (C3)

```json
{
  "retreat": {
    "quick": [
      "You bail early. No shame.",
      "You call it fast. Smart.",
      "You pivot out clean.",
      "You step back before the slip.",
      "You park the run. Fresh later.",
      "You keep the day from fraying.",
      "You back out, chin up.",
      "You wave off the grind.",
      "You pocket the lesson.",
      "You leave nothing tangled.",
      "You tap out with grace.",
      "You spare your focus.",
      "You save the rest for after.",
      "You dodge a worse idea."
    ],
    "late": [
      "You overstay. The road rolls its eyes.",
      "You limp out, pockets light.",
      "You cut losses late. Noted.",
      "You leave with scuffed pride.",
      "You spent more than you meant.",
      "You ghost the plan and pay a bit.",
      "You say ‘enough’ too late, still good.",
      "You slow‑clap yourself out.",
      "You carry the extra weight home.",
      "You learn where the edge is.",
      "You file today under ‘almost’.",
      "You pack up with a sigh.",
      "You ration the next try.",
      "You call it with a wince."
    ],
    "cursed": [
      "You bail. The day bites back.",
      "You step off and earn a chill.",
      "You rush out, mark lingers.",
      "You take the quick door, it squeaks.",
      "You trade speed for sting.",
      "You snag a curse on the frame.",
      "You leave a shadow owing you.",
      "You get tagged. It’ll fade.",
      "You sprint past a bad feeling.",
      "You dodge trouble, not the mark.",
      "You carry a small hex home.",
      "You pay a toll in quiet.",
      "You shrug off luck’s frown.",
      "You end it, but it nips."
    ]
  }
}
```

### 7.4 AMBIENT — Village & Forest (C4)

```json
{
  "ambient": {
    "VILLAGE": [
      "A cat judges your stride.",
      "Bread steam fogs a window.",
      "A cart wheel sulks in a rut.",
      "Coins clap in a palm. Not yours.",
      "Lanterns clink like soft bells.",
      "A child races a shadow and wins.",
      "You dodge a bucket, barely.",
      "A tailor pins the air with needles.",
      "You pass gossip like smoke.",
      "A dog decides you’re fine.",
      "Roofs blink in sun patches.",
      "You nod at the well stone.",
      "A door sighs you by.",
      "You borrow an alley’s cool.",
      "You step over chalk games.",
      "A baker waves with flour hands.",
      "You trade nods with a guard.",
      "Shoes click like tiny drums.",
      "Someone hums your pace.",
      "A kite pecks the clouds.",
      "You sidestep two arguing geese.",
      "A purse laughs then hushes.",
      "You share the shade with crates.",
      "Smoke writes lazy news.",
      "A bell pretends it’s urgent.",
      "You track dust like a comet.",
      "A window plants a stare on you.",
      "You collect three hellos, keep two."
    ],
    "FOREST": [
      "Wind combs the pines.",
      "A deer realizes you, then not.",
      "Sun drips in pieces.",
      "Moss edits your footsteps.",
      "Crows rearrange the air.",
      "A stream braids its own joke.",
      "Ants own the road. You yield.",
      "A pebble hums like it knows.",
      "Fern hands applaud a breeze.",
      "You taste green and grit.",
      "A stump tells rings you can’t read.",
      "Shade pockets the heat.",
      "A jay argues on your behalf.",
      "Bark files your thoughts down.",
      "Sap sweetens the silence.",
      "Web silk decides your face.",
      "A beetle drags a dream.",
      "Roots vote ‘watch your step’.",
      "A leaf rehearses its fall.",
      "Distant chops pause, listen.",
      "You share a sip with silence.",
      "Pine pitch signs your sleeve.",
      "A crow counts you twice.",
      "The path edits your plan.",
      "A cold patch keeps a secret.",
      "You borrow a sunstripe.",
      "A knot becomes a landmark.",
      "You file the forest under ‘alive’."
    ]
  }
}
```

### 7.5 CHEST — Multi‑Beat Sequences (C5)

```json
{
  "chest": {
    "empty": [
      [
        "You spot a battered coffer.",
        "Oh! A chest!",
        "You pry it open.",
        "…Empty."
      ],
      [
        "Tracks end at a crate.",
        "Could be good.",
        "Lid resists.",
        "Dust laughs at you."
      ],
      [
        "A latch winks.",
        "You grin back.",
        "Snap. Hinge groans.",
        "Nothing but splinters."
      ],
      [
        "Loose bricks hide a box.",
        "Neat.",
        "You lever it up.",
        "Space where treasure should be."
      ],
      [
        "You kick soft earth.",
        "Wood beneath.",
        "You dig fast.",
        "False bottom. No prize."
      ],
      [
        "A tarp lumps oddly.",
        "You peek.",
        "You commit.",
        "It’s only air and hope."
      ],
      [
        "Cords crisscross a bundle.",
        "You untie.",
        "You unfold.",
        "Empty, expertly."
      ],
      [
        "You read ‘keep out’.",
        "You ignore it.",
        "You open anyway.",
        "It kept out loot too."
      ],
      [
        "You find a stash niche.",
        "You feel lucky.",
        "You don’t need luck.",
        "You needed contents."
      ],
      [
        "Lid etched with swirls.",
        "Fancy.",
        "You lift, careful.",
        "It sighs. Then nothing."
      ],
      [
        "A padlock hangs broken.",
        "Promising.",
        "You pry the rest.",
        "Someone beat you here."
      ],
      [
        "Boards form a secret.",
        "You decode.",
        "You reveal.",
        "Secret is: empty."
      ]
    ],
    "loot": [
      [
        "You spot a battered coffer.",
        "Oh! A chest!",
        "You pry it open.",
        "Inside: +$GOLD gold."
      ],
      [
        "Tracks end at a crate.",
        "Could be good.",
        "Lid resists.",
        "You win +$GOLD gold."
      ],
      [
        "A latch winks.",
        "You wink back.",
        "Snap. Hinge groans.",
        "+$GOLD gold tucked in cloth."
      ],
      [
        "Loose bricks hide a box.",
        "Neat.",
        "You lever it up.",
        "Coins tumble: +$GOLD."
      ],
      [
        "You kick soft earth.",
        "Wood beneath.",
        "You dig fast.",
        "Cache spills +$GOLD gold."
      ],
      [
        "A tarp lumps oddly.",
        "You peek.",
        "You commit.",
        "Ha. +$GOLD gold, folded twice."
      ],
      [
        "Cords crisscross a bundle.",
        "You untie.",
        "You unfold.",
        "You claim +$GOLD gold."
      ],
      [
        "You read ‘keep out’.",
        "You ignore it.",
        "You open anyway.",
        "It kept +$GOLD for you."
      ],
      [
        "You find a stash niche.",
        "You feel lucky.",
        "You don’t need luck.",
        "+$GOLD says hi."
      ],
      [
        "Lid etched with swirls.",
        "Fancy.",
        "You lift, careful.",
        "Swirls frame +$GOLD gold."
      ],
      [
        "A padlock hangs broken.",
        "Promising.",
        "You pry the rest.",
        "Leftovers: +$GOLD gold."
      ],
      [
        "Boards form a secret.",
        "You decode.",
        "You reveal.",
        "Secret: +$GOLD gold."
      ]
    ]
  }
}
```

### 7.6 MOB — Outcomes (Generic + Named Overlays) (C6)

```json
{
  "mob_outcomes": {
    "generic": {
      "win": [
        "$MOB_NAME defeated. +$XP XP, +$GOLD gold.",
        "Fight tilts your way. +$XP XP, +$GOLD gold.",
        "Clean hit. Clean exit. +$XP XP, +$GOLD gold.",
        "You press, they fold. +$XP XP, +$GOLD gold.",
        "Quick read, quicker strike. +$XP XP, +$GOLD gold.",
        "Footwork first. Reward follows. +$XP XP, +$GOLD gold.",
        "You choose steady, win steady. +$XP XP, +$GOLD gold.",
        "They blink. You don’t. +$XP XP, +$GOLD gold.",
        "You make space then take it. +$XP XP, +$GOLD gold.",
        "Luck nods. You bow back. +$XP XP, +$GOLD gold.",
        "You bait, they bite. +$XP XP, +$GOLD gold.",
        "Angles, not brawn. +$XP XP, +$GOLD gold.",
        "You end it clean. +$XP XP, +$GOLD gold.",
        "You keep pace, take prize. +$XP XP, +$GOLD gold.",
        "They test your guard. Fail. +$XP XP, +$GOLD gold.",
        "You faint left, truth right. +$XP XP, +$GOLD gold.",
        "Read, react, reward. +$XP XP, +$GOLD gold.",
        "You spend nerve, earn coin. +$XP XP, +$GOLD gold.",
        "Tidy work. +$XP XP, +$GOLD gold.",
        "You outlast the noise. +$XP XP, +$GOLD gold.",
        "You win on shape, not shove. +$XP XP, +$GOLD gold.",
        "Grip holds, plan holds. +$XP XP, +$GOLD gold.",
        "Small win, solid. +$XP XP, +$GOLD gold.",
        "They fall. You breathe. +$XP XP, +$GOLD gold."
      ],
      "flee": [
        "Above your pay grade. You retreat with dignity. +$XP XP.",
        "You measure, you nope. +$XP XP.",
        "You leave speed lines. +$XP XP.",
        "Plan says ‘later’. +$XP XP.",
        "You exit before pride costs more. +$XP XP.",
        "You cut the loss clean. +$XP XP.",
        "You file this under ‘soon’. +$XP XP.",
        "You promise to come back better. +$XP XP.",
        "You spare your luck. +$XP XP.",
        "You let courage cool. +$XP XP.",
        "You trade clash for breath. +$XP XP.",
        "You draw a line and keep it. +$XP XP.",
        "You step out on purpose. +$XP XP.",
        "You live to iterate. +$XP XP.",
        "You jog the other way. +$XP XP.",
        "You blink first, wisely. +$XP XP.",
        "You ghost the fight. +$XP XP.",
        "You sign ‘not yet’. +$XP XP.",
        "You let this one pass. +$XP XP.",
        "You choose distance. +$XP XP.",
        "You back out smiling. +$XP XP.",
        "You step off, intact. +$XP XP.",
        "You skip the part with regrets. +$XP XP.",
        "You save your knuckles. +$XP XP."
      ]
    },
    "named": {
      "GOBLIN": {
        "win": [
          "Goblin drops the grin. +$XP XP, +$GOLD gold.",
          "You out‑mess a goblin. +$XP XP, +$GOLD gold.",
          "Tag taken. +$XP XP, +$GOLD gold.",
          "You pop its plan like a bubble. +$XP XP, +$GOLD gold.",
          "Goblin blinks wrong. +$XP XP, +$GOLD gold.",
          "You cut the cackle short. +$XP XP, +$GOLD gold.",
          "Shoe, meet goblin. +$XP XP, +$GOLD gold.",
          "You untangle its trap. +$XP XP, +$GOLD gold.",
          "You steal back the street. +$XP XP, +$GOLD gold.",
          "You file one less goblin. +$XP XP, +$GOLD gold."
        ],
        "flee": [
          "Goblin’s friends echo. You bail. +$XP XP.",
          "Too many eyes. Later. +$XP XP.",
          "Snare hums. You don’t. +$XP XP.",
          "You dodge the giggle and go. +$XP XP.",
          "Wrong alley. Right exit. +$XP XP.",
          "You skip the noisy ambush. +$XP XP.",
          "You keep your pockets and leave. +$XP XP.",
          "You mark this corner for ‘soon’. +$XP XP.",
          "You wave at the mess and go. +$XP XP.",
          "Cackle too eager. Pass. +$XP XP."
        ]
      },
      "WOLF": {
        "win": [
          "Wolves misread your quiet. +$XP XP, +$GOLD gold.",
          "You hold, they break. +$XP XP, +$GOLD gold.",
          "Pelt earned the hard way. +$XP XP, +$GOLD gold.",
          "You out‑pace the pack. +$XP XP, +$GOLD gold.",
          "A bark. Your answer. +$XP XP, +$GOLD gold.",
          "You guard the line. +$XP XP, +$GOLD gold.",
          "Teeth meet plan. +$XP XP, +$GOLD gold.",
          "You break the circle. +$XP XP, +$GOLD gold.",
          "Howl fades. +$XP XP, +$GOLD gold.",
          "You take the pelt with respect. +$XP XP, +$GOLD gold."
        ],
        "flee": [
          "Pack math says no. +$XP XP.",
          "Eyes count wrong. You exit. +$XP XP.",
          "Wind shifts. You do too. +$XP XP.",
          "Too many paws, not today. +$XP XP.",
          "You back off before teeth decide. +$XP XP.",
          "You keep the high ground for later. +$XP XP.",
          "You leave the chorus mid‑song. +$XP XP.",
          "You spare a pelt and go. +$XP XP.",
          "You wave at the moon and jog. +$XP XP.",
          "You pick a route with fewer teeth. +$XP XP."
        ]
      },
      "SKELETON": {
        "win": [
          "Bones learn manners. +$XP XP, +$GOLD gold.",
          "You out‑rattle the rattle. +$XP XP, +$GOLD gold.",
          "Grave quiet, then coin. +$XP XP, +$GOLD gold.",
          "You rearrange a skeleton. +$XP XP, +$GOLD gold.",
          "Rattle stops on cue. +$XP XP, +$GOLD gold.",
          "You clap dust from your hands. +$XP XP, +$GOLD gold.",
          "You win the argument with a rib. +$XP XP, +$GOLD gold.",
          "Spine gives up first. +$XP XP, +$GOLD gold.",
          "Graveyard owes you a nod. +$XP XP, +$GOLD gold.",
          "You tidy the bones. +$XP XP, +$GOLD gold."
        ],
        "flee": [
          "Cold climbs faster. You leave. +$XP XP.",
          "Bad math in the graveyard. +$XP XP.",
          "You skip a lesson in regret. +$XP XP.",
          "Rattle grows teeth. Out. +$XP XP.",
          "You save warmth for later. +$XP XP.",
          "Shadows outnumber courage. +$XP XP.",
          "You back from the stone line. +$XP XP.",
          "You spare luck here. +$XP XP.",
          "You let silence win this round. +$XP XP.",
          "You promise to return with sun. +$XP XP."
        ]
      },
      "THIEF": {
        "win": [
          "Thief blinks, you don’t. +$XP XP, +$GOLD gold.",
          "You read the feint and tax it. +$XP XP, +$GOLD gold.",
          "Pocket returned. +$XP XP, +$GOLD gold.",
          "You corner the shortcut. +$XP XP, +$GOLD gold.",
          "A wrist twist, lesson learned. +$XP XP, +$GOLD gold.",
          "You catch the grin mid‑run. +$XP XP, +$GOLD gold.",
          "You take the alley back. +$XP XP, +$GOLD gold.",
          "Plans fall out of $PRONOUN_POSS sleeves. +$XP XP, +$GOLD gold.",
          "You fence a bad idea. +$XP XP, +$GOLD gold.",
          "Market cheers quietly. +$XP XP, +$GOLD gold."
        ],
        "flee": [
          "Sneeze blows the stalk. You bolt. +$XP XP.",
          "Crowd chooses the thief. Later. +$XP XP.",
          "Too many turns. You pass. +$XP XP.",
          "You leave the chase to luck. +$XP XP.",
          "You save ankles for tomorrow. +$XP XP.",
          "Whistle warns you off. +$XP XP.",
          "You fold the tail and walk. +$XP XP.",
          "Wrong alley for heroes. +$XP XP.",
          "You wave the thief away. +$XP XP.",
          "You keep your calm and go. +$XP XP."
        ]
      },
      "SLIME": {
        "win": [
          "You out‑patient a slime. +$XP XP, +$GOLD gold.",
          "Mop technique: perfect. +$XP XP, +$GOLD gold.",
          "You part the ooze. +$XP XP, +$GOLD gold.",
          "You salt the problem. +$XP XP, +$GOLD gold.",
          "Squish surrenders. +$XP XP, +$GOLD gold.",
          "You step where it can’t. +$XP XP, +$GOLD gold.",
          "Gloop loses morale. +$XP XP, +$GOLD gold.",
          "Bucket triumph. +$XP XP, +$GOLD gold.",
          "You shear the shine off it. +$XP XP, +$GOLD gold.",
          "Drain wins. +$XP XP, +$GOLD gold."
        ],
        "flee": [
          "Wrong boots for goo. +$XP XP.",
          "You skip the slip scene. +$XP XP.",
          "Too much floor hazard. +$XP XP.",
          "You let it keep the culvert. +$XP XP.",
          "You save your soles. +$XP XP.",
          "You pass the puddle politely. +$XP XP.",
          "You grant the ooze a tie. +$XP XP.",
          "You pick dry ground today. +$XP XP.",
          "Grip says no. You agree. +$XP XP.",
          "You take the high curb. +$XP XP."
        ]
      }
    }
  }
}
```

### 7.7 QUIRKY & TRINKET (C7)

```json
{
  "quirky": [
    "An Aggro Mushroom postures. You bop it. +$XP XP.",
    "Pebble hums a note you keep. +$XP XP.",
    "You high‑five a brave squirrel. +$XP XP.",
    "A crow recruits you. You decline. +$XP XP.",
    "You win a staring match with moss. +$XP XP.",
    "You salute a scarecrow. It approves. +$XP XP.",
    "You learn a gate’s creak. +$XP XP.",
    "You balance on a curb, don’t fall. +$XP XP.",
    "You find a rhyme for ‘path’. +$XP XP.",
    "You trade hats with the wind. +$XP XP.",
    "A frog blinks in your cadence. +$XP XP.",
    "You out‑sneak your own shadow. +$XP XP.",
    "You test a shortcut, file it. +$XP XP.",
    "You time a bell, nail it. +$XP XP.",
    "You clap once, echo claps back. +$XP XP.",
    "You invent a snack break theory. +$XP XP.",
    "You name a tree ‘Maybe’. +$XP XP.",
    "You try whistling. Bird laughs. +$XP XP.",
    "You count cats. Lose track. +$XP XP.",
    "You curb a bad idea mid‑stride. +$XP XP.",
    "You gamble on a guess. Win. +$XP XP.",
    "You practice your hero nod. +$XP XP.",
    "You pass a test no one gave. +$XP XP.",
    "You pace out a plan. +$XP XP."
  ],
  "trinket": [
    "You find a curious pebble. It hums softly.",
    "A faded ribbon flutters by—lucky?",
    "You mark a safe campsite for later.",
    "A brass button winks at you.",
    "You pocket a note with no words.",
    "You coil a bit of copper wire.",
    "You rescue a good string.",
    "You keep a feather for balance.",
    "A carved bead rolls to your boot.",
    "You fold a dry leaf carefully.",
    "You stack three flat stones, grin.",
    "A washer rings like a tiny bell.",
    "You collect a neat knot.",
    "You find chalk in your pocket.",
    "A small shell refuses the sea.",
    "You press a thumbprint into clay.",
    "You save a perfect twig.",
    "A coin with a hole finds you.",
    "You keep a metal tag with nothing on it.",
    "You fix a bent nail’s mood.",
    "You rescue a sturdy pin.",
    "You pocket a lucky washer.",
    "You keep a neat brass shim.",
    "You tuck a smooth shard away."
  ]
}
```

### 7.8 SYSTEM — Curse / Level Up / Banked / Objectives (C8)

```json
{
  "system": {
    "curse_applied": [
      "Cursed: –50% for $CURSE_MINUTES min.",
      "A chill lingers: –50% ($CURSE_MINUTES m).",
      "Mark set. –50% until it fades ($CURSE_MINUTES).",
      "You carry a small hex: –50% ($CURSE_MINUTES).",
      "Penalty tag active: –50% for $CURSE_MINUTESm.",
      "Your luck limps: –50% ($CURSE_MINUTES).",
      "The road taxes you: –50% ($CURSE_MINUTES).",
      "Shadow sticks: –50% for $CURSE_MINUTES."
    ],
    "level_up": [
      "Level up.",
      "You step lighter. Level up.",
      "You stand a notch taller.",
      "Something clicks. Level up.",
      "You unlock a steadier hand.",
      "You read the road better now.",
      "You breathe and it carries farther.",
      "The path salutes your level."
    ],
    "banked": [
      "You stash your gains: +$XP XP, +$GOLD gold.",
      "Banked: +$XP XP, +$GOLD gold.",
      "You file the win: +$XP XP, +$GOLD gold.",
      "Saved for later: +$XP XP, +$GOLD gold.",
      "Ledger notes: +$XP XP, +$GOLD gold.",
      "You put today on the shelf: +$XP, +$GOLD.",
      "You tuck rewards away: +$XP, +$GOLD.",
      "Totals banked: +$XP XP, +$GOLD gold."
    ],
    "objective_progress": [
      "$OBJ_NAME: $OBJ_PROGRESS/$OBJ_TOTAL",
      "Progress — $OBJ_NAME: $OBJ_PROGRESS/$OBJ_TOTAL",
      "Tick — $OBJ_NAME: $OBJ_PROGRESS/$OBJ_TOTAL",
      "$OBJ_NAME moved to $OBJ_PROGRESS/$OBJ_TOTAL",
      "$OBJ_NAME counts: $OBJ_PROGRESS of $OBJ_TOTAL",
      "$OBJ_NAME now $OBJ_PROGRESS/$OBJ_TOTAL",
      "$OBJ_NAME nudged: $OBJ_PROGRESS/$OBJ_TOTAL",
      "$OBJ_NAME updated: $OBJ_PROGRESS/$OBJ_TOTAL"
    ],
    "objective_complete": [
      "$OBJ_NAME complete.",
      "$OBJ_NAME wrapped.",
      "$OBJ_NAME done and dusted.",
      "$OBJ_NAME checked off.",
      "$OBJ_NAME: complete.",
      "$OBJ_NAME closed.",
      "$OBJ_NAME says thanks.",
      "$OBJ_NAME finished."
    ],
    "chapter_unlocked": [
      "$CHAPTER_TITLE unlocked.",
      "New chapter: $CHAPTER_TITLE.",
      "$CHAPTER_TITLE opens ahead.",
      "The road points to $CHAPTER_TITLE.",
      "Chapter unlocked — $CHAPTER_TITLE.",
      "Next up: $CHAPTER_TITLE.",
      "$CHAPTER_TITLE is now live.",
      "You earn a new path: $CHAPTER_TITLE."
    ]
  }
}
```

---

## 8) Event → Thread Mapping (D1)

| Event Type         | Thread Title             | Beat Plan                                                                                            |
|--------------------|--------------------------|------------------------------------------------------------------------------------------------------|
| **START**          | "Start" (class‑aware)    | 1× from Openers (`common` + class overlay).                                                          |
| **MOB Win**        | `$MOB_NAME` (with level) | 1× Narration (tension), 1× Battle (engage), 1× Outcome from `mob_outcomes` (named→generic fallback). |
| **MOB Flee**       | `$MOB_NAME` (with level) | 1× Narration (assess), 1× System (level gate hint optional), 1× Flee line from `mob_outcomes`.       |
| **CHEST loot**     | "Chest"                  | 3–4 steps from `chest.loot` variant. Final beat includes `+$GOLD`.                                   |
| **CHEST empty**    | "Chest"                  | 3–4 steps from `chest.empty`.                                                                        |
| **QUIRKY**         | "Quirk"                  | 1× from `quirky` (carry `+$XP`).                                                                     |
| **TRINKET**        | "Trinket"                | 1× from `trinket`.                                                                                   |
| **VISIT**          | `$PLACE`                 | 1–2× Narration beats; if tied to objective, add System progress line.                                |
| **SYSTEM: banked** | "Tally"                  | 1× System from `banked`.                                                                             |
| **SYSTEM: curse**  | "Mark"                   | 1× System from `curse_applied`.                                                                      |
| **FINISH**         | "Finish"                 | 1–2× from `closers`.                                                                                 |
| **RETREAT**        | "Retreat"                | 1× from `retreat.quick/late/cursed` based on context; optional System.                               |

**Overlay rules**

- Named MOB overlays take priority; fall back to generic lines.
- Under‑level gates (Coordinator can tag): use FLEE outcome.
- Story encounters (reskins) still map through MOB; thread title becomes the encounter name.

---

## 9) Notification & UI Microcopy

### 9.1 Ongoing Notification Snippets (E1)

```json
{
  "ongoing_snippets": [
    "On the road.",
    "Eyes up, steps steady.",
    "Tracking the beat.",
    "You’ve got this.",
    "Quiet grind.",
    "Making small work big.",
    "Keeping pace.",
    "Staying with it.",
    "Neat work in progress.",
    "Path agrees with you.",
    "Hands know what to do.",
    "Still moving.",
    "Focus holds.",
    "Good rhythm.",
    "Small steps, loud future.",
    "Breathing through it.",
    "Clean reps.",
    "You and the road.",
    "Light on your feet.",
    "Work first, noise later.",
    "Pace is yours.",
    "On task.",
    "Neatly done.",
    "In the pocket."
  ]
}
```

### 9.2 Objective Chip Patterns (E2)

```json
{
  "objective_chip": [
    "Tags $OBJ_PROGRESS/$OBJ_TOTAL",
    "Pelts $OBJ_PROGRESS/$OBJ_TOTAL",
    "Bridge $OBJ_PROGRESS/$OBJ_TOTAL",
    "Grave check $OBJ_PROGRESS/$OBJ_TOTAL",
    "Alley watch $OBJ_PROGRESS/$OBJ_TOTAL",
    "Culvert clear $OBJ_PROGRESS/$OBJ_TOTAL",
    "Forest pass $OBJ_PROGRESS/$OBJ_TOTAL",
    "Lookouts $OBJ_PROGRESS/$OBJ_TOTAL",
    "Footbridge $OBJ_PROGRESS/$OBJ_TOTAL",
    "Pelts done $OBJ_PROGRESS/$OBJ_TOTAL",
    "Tags done $OBJ_PROGRESS/$OBJ_TOTAL",
    "Errands $OBJ_PROGRESS/$OBJ_TOTAL"
  ]
}
```

---

## 10) Analytics (F1)

```json
{
  "analytics": [
    {
      "name": "story_thread_emitted",
      "properties": [
        "quest_id",
        "kind",
        "source",
        "ref_idx",
        "biome",
        "def_id"
      ]
    },
    {
      "name": "story_beat_displayed",
      "properties": [
        "thread_id",
        "idx",
        "kind"
      ]
    },
    {
      "name": "objective_progressed",
      "properties": [
        "objective_id",
        "delta",
        "count",
        "total"
      ]
    },
    {
      "name": "objective_completed",
      "properties": [
        "objective_id"
      ]
    },
    {
      "name": "chapter_unlocked",
      "properties": [
        "chapter_id"
      ]
    },
    {
      "name": "ambient_fired",
      "properties": [
        "quest_id",
        "def_id",
        "biome"
      ]
    },
    {
      "name": "encounter_reskinned",
      "properties": [
        "quest_id",
        "ref_idx",
        "mob",
        "encounter_id"
      ]
    },
    {
      "name": "notification_line_updated",
      "properties": [
        "thread_id",
        "beat_idx"
      ]
    }
  ]
}
```

---

## 11) QA / Validation (G1, G2)

Use these checks before seeding:

- Max line length ≤120 chars; ≥90% ≤80 chars.
- No duplicates (case/punct‑insensitive).
- No forbidden words in Ch.1: “dragon”, “dragons”, “wizard”.
- No modern slang.

**Prompt — Style audit (returns JSON):**

```
Given the catalogs JSON, return a report of violations: length >120, >10% of lines >80 chars,
duplicates (case/punct-insensitive), forbidden words (“dragon”, “dragons”, “wizard”), modern slang.
Output: {"violations":[{"key":"openers.common[12]","issue":"duplicate","text":"..."}]}
```

**Prompt — Auto‑fix patch (returns JSON):**

```
Given the same JSON + violations, return a patch of {path, replacement} for only flagged lines.
```

---

## 12) Tests (I1, I2)

### 12.1 Determinism & Parity (table)

| Case              | Inputs                     | Expected                                                   |
|-------------------|----------------------------|------------------------------------------------------------|
| Same seed ambient | baseSeed=123, quest 25m    | Same ambient windows & fired defs, same text order.        |
| Reskin gate       | MOB=GOBLIN major, prob hit | Encounter thread uses Goblin Lookout; titles/lines stable. |
| Under‑level flee  | MOB=SKELETON at hero+3     | FLEE outcome, gold=0, flee line used.                      |
| Ledger parity     | Allocated XP/GOLD          | Sum of MOB win outcomes equals ledger deltas.              |
| Process death     | Kill app mid‑run           | Threads reload from DB; latest beat = notification line.   |
| Perf no spam      | 60m quest                  | Emits only on events/ambients; no per‑sec writes.          |

### 12.2 Objective Progression (JSON)

```json
{
  "tests": [
    {
      "name": "goblin_kill_3",
      "events": [
        {
          "type": "MOB",
          "mob": "GOBLIN",
          "outcome": "WIN"
        },
        {
          "type": "MOB",
          "mob": "GOBLIN",
          "outcome": "WIN"
        },
        {
          "type": "MOB",
          "mob": "GOBLIN",
          "outcome": "WIN"
        }
      ],
      "expected": {
        "obj_goblin_tags": 3
      }
    },
    {
      "name": "wolf_pelt_drops",
      "events": [
        {
          "type": "MOB",
          "mob": "WOLF",
          "outcome": "WIN",
          "drop": "WOLF_PELT"
        },
        {
          "type": "MOB",
          "mob": "WOLF",
          "outcome": "WIN"
        },
        {
          "type": "MOB",
          "mob": "WOLF",
          "outcome": "WIN",
          "drop": "WOLF_PELT"
        }
      ],
      "expected": {
        "obj_wolf_pelts": ">=2"
      }
    },
    {
      "name": "visit_footbridge",
      "events": [
        {
          "type": "VISIT",
          "place": "FOOTBRIDGE"
        }
      ],
      "expected": {
        "obj_visit_footbridge": 1
      }
    }
  ]
}
```

---

## 13) Seeding & Migration

1) Apply schema migration (Section 4 tables).
2) Insert **Chapter 1** JSON (6.1) into `chapters` and `objectives`.
3) Insert **Ambient** and **Encounter** defs (6.2, 6.3) into `story_event_defs` with `trigger_rules_json` and
   `payload_json` as serialized JSON.
4) Load **Beat catalogs** (Section 7) into your content store or ship as static assets used by `StoryEmitter` keyed by
   `beats_key`.
5) Version content pack (`pack_id=ch1_v1`).

**Rollback:** drop new tables; ledger/quest tables unaffected.

---

## 14) UI Notes

- Adventure Log = **series of Event Threads**; latest expanded.
- Filter chips: All • Battles • Loot • Quirks • Story.
- Portal ticker = latest beat; ongoing notification mirrors it.
- Chapter panel shows objective chips and bars; tap to open.

---

## 15) Future Chapters (stubs only)

- Keep structure; raise stakes slowly.
- Introduce mentors (elder/scribe) before power figures.
- Add biomes: Ruins, Riverbank, Quarry.
- New objective types later: CRAFT, DIALOG (lightweight), ESCORT (narrative).

---

**End of README**

