package org.umlreviewer.enums

enum class DiagramIssueGroup(val issues: Set<DiagramIssue>) {
    UseCaseDiagram(setOf(
        DiagramIssue.ISSUE_11, DiagramIssue.ISSUE_12, DiagramIssue.ISSUE_13, DiagramIssue.ISSUE_14,
        DiagramIssue.ISSUE_15, DiagramIssue.ISSUE_16, DiagramIssue.ISSUE_17, DiagramIssue.ISSUE_18,
        DiagramIssue.ISSUE_19, DiagramIssue.ISSUE_110, DiagramIssue.ISSUE_111, DiagramIssue.ISSUE_112,
        DiagramIssue.ISSUE_113, DiagramIssue.ISSUE_114, DiagramIssue.ISSUE_115, DiagramIssue.ISSUE_116,
        DiagramIssue.ISSUE_117, DiagramIssue.ISSUE_118
    )),

    ActivityDiagram(setOf(
        DiagramIssue.ISSUE_21, DiagramIssue.ISSUE_22, DiagramIssue.ISSUE_23, DiagramIssue.ISSUE_24,
        DiagramIssue.ISSUE_25, DiagramIssue.ISSUE_26, DiagramIssue.ISSUE_27, DiagramIssue.ISSUE_28,
        DiagramIssue.ISSUE_29, DiagramIssue.ISSUE_30
    )),

    AnalyticalClassDiagram(setOf(
        DiagramIssue.ISSUE_31, DiagramIssue.ISSUE_32, DiagramIssue.ISSUE_33, DiagramIssue.ISSUE_34,
        DiagramIssue.ISSUE_35, DiagramIssue.ISSUE_36, DiagramIssue.ISSUE_37, DiagramIssue.ISSUE_38,
        DiagramIssue.ISSUE_39, DiagramIssue.ISSUE_310, DiagramIssue.ISSUE_311, DiagramIssue.ISSUE_312,
        DiagramIssue.ISSUE_313, DiagramIssue.ISSUE_314, DiagramIssue.ISSUE_315, DiagramIssue.ISSUE_316,
        DiagramIssue.ISSUE_317, DiagramIssue.ISSUE_318, DiagramIssue.ISSUE_319, DiagramIssue.ISSUE_320,
        DiagramIssue.ISSUE_321, DiagramIssue.ISSUE_322, DiagramIssue.ISSUE_323, DiagramIssue.ISSUE_324,
        DiagramIssue.ISSUE_325, DiagramIssue.ISSUE_326,
    )),

    StateDiagram(setOf(
        DiagramIssue.ISSUE_41, DiagramIssue.ISSUE_42, DiagramIssue.ISSUE_43, DiagramIssue.ISSUE_44,
        DiagramIssue.ISSUE_45, DiagramIssue.ISSUE_46, DiagramIssue.ISSUE_47, DiagramIssue.ISSUE_48,
        DiagramIssue.ISSUE_49, DiagramIssue.ISSUE_410, DiagramIssue.ISSUE_411, DiagramIssue.ISSUE_412,
        DiagramIssue.ISSUE_413, DiagramIssue.ISSUE_414, DiagramIssue.ISSUE_415,
    )),

    EntityRelationshipDiagram(setOf(
        DiagramIssue.ISSUE_51, DiagramIssue.ISSUE_52, DiagramIssue.ISSUE_53, DiagramIssue.ISSUE_54,
        DiagramIssue.ISSUE_55, DiagramIssue.ISSUE_56, DiagramIssue.ISSUE_57, DiagramIssue.ISSUE_58,
        DiagramIssue.ISSUE_59,
    )),

    DesignClassDiagram(setOf(
        DiagramIssue.ISSUE_61, DiagramIssue.ISSUE_62, DiagramIssue.ISSUE_63, DiagramIssue.ISSUE_64,
        DiagramIssue.ISSUE_65, DiagramIssue.ISSUE_66, DiagramIssue.ISSUE_67, DiagramIssue.ISSUE_68,
        DiagramIssue.ISSUE_69, DiagramIssue.ISSUE_610, DiagramIssue.ISSUE_611, DiagramIssue.ISSUE_612,
        DiagramIssue.ISSUE_613, DiagramIssue.ISSUE_614, DiagramIssue.ISSUE_615, DiagramIssue.ISSUE_616,
        DiagramIssue.ISSUE_617, DiagramIssue.ISSUE_618, DiagramIssue.ISSUE_619, DiagramIssue.ISSUE_620,
        DiagramIssue.ISSUE_621, DiagramIssue.ISSUE_622, DiagramIssue.ISSUE_623, DiagramIssue.ISSUE_624,
        DiagramIssue.ISSUE_625, DiagramIssue.ISSUE_626, DiagramIssue.ISSUE_627, DiagramIssue.ISSUE_628,
    )),

    SequenceDiagram(setOf(
        DiagramIssue.ISSUE_71, DiagramIssue.ISSUE_72, DiagramIssue.ISSUE_73, DiagramIssue.ISSUE_74,
        DiagramIssue.ISSUE_75, DiagramIssue.ISSUE_76, DiagramIssue.ISSUE_77, DiagramIssue.ISSUE_78,
        DiagramIssue.ISSUE_79, DiagramIssue.ISSUE_710, DiagramIssue.ISSUE_711, DiagramIssue.ISSUE_712,
        DiagramIssue.ISSUE_713, DiagramIssue.ISSUE_714, DiagramIssue.ISSUE_715, DiagramIssue.ISSUE_716,
        DiagramIssue.ISSUE_717, DiagramIssue.ISSUE_718, DiagramIssue.ISSUE_719, DiagramIssue.ISSUE_720,
        DiagramIssue.ISSUE_721, DiagramIssue.ISSUE_722, DiagramIssue.ISSUE_723, DiagramIssue.ISSUE_724,
        DiagramIssue.ISSUE_725, DiagramIssue.ISSUE_726,
    )),

    CommunicationDiagram(setOf(
        DiagramIssue.ISSUE_81, DiagramIssue.ISSUE_82, DiagramIssue.ISSUE_83, DiagramIssue.ISSUE_84,
        DiagramIssue.ISSUE_85, DiagramIssue.ISSUE_86, DiagramIssue.ISSUE_87, DiagramIssue.ISSUE_88,
        DiagramIssue.ISSUE_89, DiagramIssue.ISSUE_810, DiagramIssue.ISSUE_811, DiagramIssue.ISSUE_812,
        DiagramIssue.ISSUE_813, DiagramIssue.ISSUE_814,
    ));

    companion object {
        fun getGroup(issue: DiagramIssue): DiagramIssueGroup? {
            return values().find { it.issues.contains(issue) }
        }
    }
}
