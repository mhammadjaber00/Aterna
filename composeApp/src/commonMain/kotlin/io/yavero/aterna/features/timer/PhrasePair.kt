package io.yavero.aterna.features.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import aterna.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

data class PhrasePair(val start: StringResource, val dismiss: StringResource)

private val TIMER_PHRASE_PAIRS: List<PhrasePair> = listOf(
    // -------- Doctor Who --------
    PhrasePair(Res.string.start_allons_y, Res.string.dismiss_not_now),
    PhrasePair(Res.string.start_geronimo, Res.string.dismiss_not_today_daleks),

    // -------- Lord of the Rings --------
    PhrasePair(Res.string.start_for_frodo, Res.string.dismiss_second_breakfast),
    PhrasePair(Res.string.start_fly_you_fools, Res.string.dismiss_shire_wait),
    PhrasePair(Res.string.start_to_mordor, Res.string.dismiss_with_the_hobbits),
    PhrasePair(Res.string.start_one_quest_rule, Res.string.dismiss_not_all_wander),

    // -------- Game of Thrones --------
    PhrasePair(Res.string.start_dracarys, Res.string.dismiss_i_drink_cancel),
    PhrasePair(Res.string.start_winter_is_coming, Res.string.dismiss_not_today),
    PhrasePair(Res.string.start_i_choose_violence, Res.string.dismiss_hold_the_door),
    PhrasePair(Res.string.start_what_is_dead, Res.string.dismiss_night_is_dark),

    // -------- General RPG / Adventure --------
    PhrasePair(Res.string.start_adventure_time, Res.string.dismiss_left_oven_on),
    PhrasePair(Res.string.start_begin_run, Res.string.dismiss_another_time),
    PhrasePair(Res.string.start_begin_the_quest, Res.string.dismiss_maybe_later),
    PhrasePair(Res.string.start_engage, Res.string.dismiss_on_second_thought),
    PhrasePair(Res.string.start_enter_the_dungeon, Res.string.dismiss_goblins_can_wait),
    PhrasePair(Res.string.start_focus_mode, Res.string.dismiss_my_cat_needs_me),
    PhrasePair(Res.string.start_forge_ahead, Res.string.dismiss_duty_elsewhere),
    PhrasePair(Res.string.start_light_the_torch, Res.string.dismiss_must_meditate),
    PhrasePair(Res.string.start_lock_in, Res.string.dismiss_cancel_for_now),
    PhrasePair(Res.string.start_lets_roll, Res.string.dismiss_back_to_bed),
    PhrasePair(Res.string.start_onward, Res.string.dismiss_stay_in_town),
    PhrasePair(Res.string.start_push_forward, Res.string.dismiss_i_choose_later),
    PhrasePair(Res.string.start_raise_the_banner, Res.string.dismiss_need_snacks),
    PhrasePair(Res.string.start_saddle_up, Res.string.dismiss_low_on_mana),
    PhrasePair(Res.string.start_seal_the_ritual, Res.string.dismiss_timey_wimey),
    PhrasePair(Res.string.start_take_the_quest, Res.string.dismiss_forgot_my_sword),
    PhrasePair(Res.string.start_to_glory, Res.string.dismiss_too_risky),
    PhrasePair(Res.string.start_to_the_grindstone, Res.string.dismiss_rain_check),

    // -------- Extra to use every dismiss once (duplicates on starts) --------
    PhrasePair(Res.string.start_raise_the_banner, Res.string.dismiss_tavern_calls),   // duplicate start
    PhrasePair(Res.string.start_enter_the_dungeon, Res.string.dismiss_upside_down_map), // duplicate start
    PhrasePair(Res.string.start_lock_in, Res.string.dismiss_im_out),          // duplicate start
)

@Composable
fun rememberTimerPhrasePair(): PhrasePair {
    return remember { TIMER_PHRASE_PAIRS.random() }
}
