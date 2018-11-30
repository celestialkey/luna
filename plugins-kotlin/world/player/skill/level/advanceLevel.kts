import AdvanceLevel.LevelUpInterface
import api.*
import io.luna.game.event.impl.SkillChangeEvent
import io.luna.game.model.mob.Graphic
import io.luna.game.model.mob.Player
import io.luna.game.model.mob.Skill
import io.luna.game.model.mob.block.UpdateFlagSet.UpdateFlag
import io.luna.game.model.mob.inter.DialogueInterface
import io.luna.net.msg.out.SkillUpdateMessageWriter
import io.luna.util.StringUtils

/**
 * A model representing [LevelUpInterface] data.
 */
data class LevelUpData(val firstLine: Int,
                       val secondLine: Int,
                       val inter: Int)

/**
 * A [DialogueInterface] that opens when a level is advanced.
 */
class LevelUpInterface(private val skill: Int,
                       private val newLevel: Int,
                       private val data: LevelUpData) : DialogueInterface(data.inter) {
    override fun onOpen(player: Player) {
        val skillName = Skill.getName(skill)
        val lvlUpMessage = "Congratulations, you just advanced ${StringUtils.addArticle(skillName)} level!"

        player.sendMessage(lvlUpMessage)
        player.sendText(lvlUpMessage, data.firstLine)
        player.sendText("Your $skillName level is now $newLevel.", data.secondLine)
    }
}

/**
 * Graphic played when a player advances a level.
 */
private val fireworksGraphic = Graphic(199)

/**
 * A table holding data for the [LevelUpInterface].
 */
private val levelUpTable = immutableListOf(
        LevelUpData(6248, 6249, 6247),
        LevelUpData(6254, 6255, 6253),
        LevelUpData(6207, 6208, 6206),
        LevelUpData(6217, 6218, 6216),
        LevelUpData(5453, 6114, 4443),
        LevelUpData(6243, 6244, 6242),
        LevelUpData(6212, 6213, 6211),
        LevelUpData(6227, 6228, 6226),
        LevelUpData(4273, 4274, 4272),
        LevelUpData(6232, 6233, 6231),
        LevelUpData(6259, 6260, 6258),
        LevelUpData(4283, 4284, 4282),
        LevelUpData(6264, 6265, 6263),
        LevelUpData(6222, 6223, 6221),
        LevelUpData(4417, 4438, 4416),
        LevelUpData(6238, 6239, 6237),
        LevelUpData(4278, 4279, 4277),
        LevelUpData(4263, 4264, 4261),
        LevelUpData(12123, 12124, 12122),
        LevelUpData(4889, 4890, 4887),
        LevelUpData(4268, 4269, 4267))

/**
 * Determine if a player has advanced a level. If they have, send congratulatory messages.
 */
fun advanceLevel(plr: Player, skillId: Int, oldLevel: Int) {
    val skill = plr.skill(skillId)
    val newLevel = skill.staticLevel
    if (oldLevel < newLevel) {
        skill.level = when (skillId) {
            SKILL_HITPOINTS -> skill.level + 1
            else -> newLevel
        }

        plr.interfaces.open(LevelUpInterface(skillId, newLevel, levelUpTable[skillId]))
        plr.graphic(fireworksGraphic)

        if (Skill.isCombatSkill(skillId)) {
            plr.skills.resetCombatLevel()
            plr.flag(UpdateFlag.APPEARANCE)
        }
    }
}

/**
 * When a player's skills change, send the update to the client and check if they've advanced a
 * level.
 */
on(SkillChangeEvent::class)
    .condition { it.mob.type == TYPE_PLAYER }
    .run {
        val plr = it.plr
        val skill = it.id
        val staticLevel = it.oldStaticLvl
        plr.queue(SkillUpdateMessageWriter(skill))
        if (staticLevel < 99) {
            advanceLevel(plr, skill, staticLevel)
        }
    }