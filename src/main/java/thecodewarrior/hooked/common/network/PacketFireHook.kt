package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.HookLog
import thecodewarrior.hooked.common.capability.HooksCap
import thecodewarrior.hooked.common.hook.HookInFlight
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class PacketFireHook : PacketBase() {

    @Save
    var pos: Vec3d = Vec3d.ZERO
    @Save
    var normal: Vec3d = Vec3d.ZERO
    @Save
    var uuid: UUID = UUID.randomUUID()

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.player)
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            val spawnDistance = player.positionVector.distanceTo(pos)
            if(spawnDistance > 10) {
                HookLog.warn("Player ${player.name} spawned a hook too far from their body! Expected point within " +
                        "10 blocks of player. Got $pos, $spawnDistance blocks away.")
            }

            val controller = cap.controller ?: return@ifCap
            controller.fireHook(pos, normal.normalize(), uuid)

            cap.update()
        }
    }
}
