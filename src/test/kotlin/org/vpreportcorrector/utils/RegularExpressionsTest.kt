package org.vpreportcorrector.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.vpreportcorrector.utils.FileTreeHelpers.REGEX_TEAM_FOLDER
import org.vpreportcorrector.utils.FileTreeHelpers.REGEX_WEEK_FOLDER


internal class RegularExpressionsTest {

    @Nested
    inner class WeekRegex {
        private fun testShouldMatch(strings: List<String>) {
            strings.forEach {
                assertTrue(REGEX_WEEK_FOLDER.matches(it), "Expression should match '${it}'.")
            }
        }
        private fun testShouldNotMatch(strings: List<String>) {
            strings.forEach {
                assertFalse(REGEX_WEEK_FOLDER.matches(it), "Expression should NOT match '${it}'.")
            }
        }
        @Test
        fun `should match english, czech and slovak 'week' with number in the front`() {
            testShouldMatch(
                listOf(
                    "1 week", "1week", "0001   week", "0001 week", "0001week", "234week",
                    "1 týden", "1týden", "0001   týden", "0001 týden", "0001týden", "234týden",
                    "1 tyden", "1tyden", "0001   tyden", "0001 tyden", "0001tyden", "234tyden",
                    "1 týždeň", "1týždeň", "0001   týždeň", "0001 týždeň", "0001týždeň", "234týždeň",
                    "1 tyzden", "1tyzden", "0001   tyzden", "0001 tyzden", "0001tyzden", "234tyzden",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'week' with number in the front case insensitive`() {
            testShouldMatch(
                listOf(
                    "1 WEEK", "1weEk",
                    "1 TÝDEN", "1Týden", "0001   týDen",
                    "1 TYDEN", "1Tyden", "0001   tyDen",
                    "1 TÝŽDEŇ", "1Týždeň", "0001   týžDeň",
                    "1 TYZDEN", "1Tyzden", "0001   tyzDen",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'week' with number in the back`() {
            testShouldMatch(
                listOf(
                    "week 1", "week1", "week   21", "week0001", "week 001", "week   001",
                    "týden 1", "týden1", "týden   21", "týden0001", "týden 001", "týden   001",
                    "tyden 1", "tyden1", "tyden   21", "tyden0001", "tyden 001", "tyden   001",
                    "týždeň 1", "týždeň1", "týždeň   21", "týždeň0001", "týždeň 001", "týždeň   001",
                    "tyzden 1", "tyzden1", "tyzden   21", "tyzden0001", "tyzden 001", "tyzden   001",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'week' with number in the back case insensitive`() {
            testShouldMatch(
                listOf(
                    "WEEK 1", "weEk1",
                    "TÝDEN 1", "tÝden1",
                    "TYDEN 1", "tydEn1",
                    "TÝŽDEŇ 1", "týždEň1",
                    "TYZDEN 6541", "tyzDen001",
                )
            )
        }

        @Test
        fun `should also accept '_' and '-' as separators`() {
            testShouldMatch(
                listOf(
                    "1_week", "1-week",
                    "1__týden", "1--týden",
                    "tyden_1", "tyden-1",
                    "týždeň___1", "týždeň---1",
                )
            )
        }

        @Test
        fun `should not match 'week' in either language with multiple numbers in the front`() {
            testShouldNotMatch(
                listOf(
                    "1 48 005 week",
                    "1 14 týden",
                    "1 465tyden",
                    "1 004 týždeň",
                    "001 004tyzden",
                )
            )
        }

        @Test
        fun `should not match 'week' in either language with multiple numbers in the back`() {
            testShouldNotMatch(
                listOf(
                    "week 1 54", "week15 35",
                    "týden 54 48", "týden864 85",
                    "tyden-48-84", "tyden64-87",
                    "týždeň_15_165_005", "týždeň165_48",
                )
            )
        }

        @Test
        fun `should not match 'week' in either language with multiple numbers in the front and back`() {
            testShouldNotMatch(listOf(
                "51 654week 464 1", "01 week1 6546", "011week   21  654",
            ))
        }

        @Test
        fun `should not match strings without a number with 'week' in either language`() {
            testShouldNotMatch(listOf(
                "week", "týden", "týždeň", "tyden", "tyzden"
            ))
        }

        @Test
        fun `should not match strings not containing correct 'week' in either language`() {
            testShouldNotMatch(listOf(
                "salehfs iusdhf ksh 145", "year 4", "14 year", "14 wLeLeLk", "15 w e e k",
                "t y d e n 15", "w-e-e-k", "t_ý_d_e_n", "15 t ý ž d e ň", "001t y d e n", "t-y-z-d-e-n 15",
            ))
        }
    }

    @Nested
    inner class TeamRegex {
        private fun testShouldMatch(strings: List<String>) {
            strings.forEach {
                assertTrue(REGEX_TEAM_FOLDER.matches(it), "Expression should match '${it}'.")
            }
        }
        private fun testShouldNotMatch(strings: List<String>) {
            strings.forEach {
                assertFalse(REGEX_TEAM_FOLDER.matches(it), "Expression should NOT match '${it}'.")
            }
        }
        @Test
        fun `should match english, czech and slovak 'team' with number in the front`() {
            testShouldMatch(
                listOf(
                    "1 team", "1team", "0001   team", "0001 team", "0001team", "234team",
                    "1 tým", "1tým", "0001   tým", "0001 tým", "0001tým", "234tým",
                    "1 tym", "1tym", "0001   tym", "0001 tym", "0001tym", "234tym",
                    "1 tím", "1tím", "0001   tím", "0001 tím", "0001tím", "234tím",
                    "1 tim", "1tim", "0001   tim", "0001 tim", "0001tim", "234tim",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'team' with number in the front case insensitive`() {
            testShouldMatch(
                listOf(
                    "1 TEaM", "1teAm",
                    "1 TÝM", "1tÝm",
                    "1 TYM", "1Tym",
                    "1 TÍM", "1TíM",
                    "1 TIM", "1TiM",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'team' with number in the back`() {
            testShouldMatch(
                listOf(
                    "team 1", "team1", "team   21", "team0001", "team 001", "team   001",
                    "tým 1", "tým1", "tým   21", "tým0001", "tým 001", "tým   001",
                    "tym 1", "tym1", "tym   21", "tym0001", "tym 001", "tym   001",
                    "tím 1", "tím1", "tím   21", "tím0001", "tím 001", "tím   001",
                    "tim 1", "tim1", "tim   21", "tim0001", "tim 001", "tim   001",
                )
            )
        }

        @Test
        fun `should match english, czech and slovak 'team' with number in the back case insensitive`() {
            testShouldMatch(
                listOf(
                    "TEAM 005", "teAm005",
                    "TÝM 5", "tÝm  005",
                    "TYM 25", "Tym 255",
                    "TÍM 005", "TíM5",
                    "TIM 005", "TiM447",
                )
            )
        }

        @Test
        fun `should also accept '_' and '-' as separators`() {
            testShouldMatch(
                listOf(
                    "1_team", "1-team",
                    "1__tým", "1--tým",
                    "tím_1", "tím-1",
                    "tim___1", "tim---1",
                )
            )
        }

        @Test
        fun `should not match 'team' in either language with multiple numbers in the front`() {
            testShouldNotMatch(
                listOf(
                    "1 48 005 team",
                    "1 14 tým",
                    "1_465tym",
                    "1-004-tím",
                    "001 004tim",
                )
            )
        }

        @Test
        fun `should not match 'team' in either language with multiple numbers in the back`() {
            testShouldNotMatch(
                listOf(
                    "team 1 54", "team15 35",
                    "tým 54 48", "tým864 85",
                    "tym-48-84", "tym64-87",
                    "tím_15_165_005", "tím165_48",
                    "tim_15_165_005", "tim165_48",
                )
            )
        }

        @Test
        fun `should not match 'team' in either language with multiple numbers in the front and back`() {
            testShouldNotMatch(listOf(
                "51 654team 464 1", "01 team1 6546", "011team   21  654",
            ))
        }

        @Test
        fun `should not match strings without a number with 'team' in either language`() {
            testShouldNotMatch(listOf(
                "team", "tým", "tym", "tím", "tim"
            ))
        }

        @Test
        fun `should not match strings not containing correct 'team' in either language`() {
            testShouldNotMatch(listOf(
                "salehfs iusdhf ksh 145", "year 4", "14 year", "14 t e a m", "15 t_e_a_m",
                "t ý m 15", "t í m 684", "t_i_m", "15 t ý ž d e ň", "001t y d e n", "t-y-z-d-e-n 15",
                "week 01",
            ))
        }
    }
}
