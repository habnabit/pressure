/*
 * Copyright (c) bdew, 2013 - 2015
 * https://github.com/bdew/pressure
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.pressure.blocks.tank.blocks

import java.util.Locale

import dan200.computercraft.api.ComputerCraftAPI
import net.bdew.lib.multiblock.data.{OutputConfigFluid, OutputConfigRSControllable, RSMode}
import net.bdew.lib.multiblock.tile.TileModule
import net.bdew.pressure.blocks.tank.BaseModule
import net.bdew.pressure.blocks.tank.controller.TileTankController
import net.bdew.pressure.compat.computercraft._
import net.minecraftforge.fluids.FluidRegistry

object BlockDataPort extends BaseModule("TankDataPort", "TankDataPort", classOf[TileDataPort]) {
  ComputerCraftAPI.registerPeripheralProvider(new TilePeripheralProvider("tank_dataport", DataPortCommands, classOf[TileDataPort]))
}

class TileDataPort extends TileModule {
  val kind: String = "TankDataPort"

  override def getCore = getCoreAs[TileTankController]
}

object DataPortCommands extends TileCommandHandler[TileDataPort] {
  val outputNames = Map(
    "red" -> 0,
    "green" -> 1,
    "blue" -> 2,
    "yellow" -> 3,
    "cyan" -> 4,
    "purple" -> 5
  )

  def getCore(ctx: CallContext[TileDataPort]) =
    ctx.tile.getCore.getOrElse(err("Not connected to tank"))

  command("isConnected") { ctx =>
    ctx.tile.getCore.isDefined
  }

  command("getCapacity") { ctx =>
    getCore(ctx).tank.getCapacity
  }

  command("hasFluid") { ctx =>
    val tank = getCore(ctx).tank
    tank.getFluid != null && tank.getFluid.amount > 0
  }

  command("getFluid") { ctx =>
    val fluid = getCore(ctx).tank.getFluid
    if (fluid != null && fluid.getFluid != null && fluid.amount > 0)
      CCResult.Map(
        "name" -> fluid.getFluid.getName,
        "amount" -> fluid.amount
      )
    else
      CCResult.Null
  }

  command("getOutputs") { ctx =>
    val configs = getCore(ctx).outputConfig
    for ((oName, oNum) <- outputNames) yield {
      oName -> (configs.get(oNum) match {
        case Some(x: OutputConfigFluid) =>
          CCResult.Map(
            "type" -> "fluid",
            "mode" -> x.rsMode.toString,
            "average" -> x.avg
          )
        case None => CCResult.Map("type" -> "unconnected")
        case _ => CCResult.Map("type" -> "unknown")
      })
    }
  }

  command("setOutputMode") { ctx =>
    val (oName, mode) = ctx.params(CCString, CCString)
    val oNum = outputNames.getOrElse(oName.toLowerCase(Locale.US), err("Invalid output name"))
    val newMode = try {
      RSMode.withName(mode.toUpperCase(Locale.US))
    } catch {
      case e: NoSuchElementException => err("Invalid output mode")
    }
    getCore(ctx).outputConfig.get(oNum) match {
      case Some(x: OutputConfigRSControllable) =>
        x.rsMode = newMode
        getCore(ctx).outputConfig.updated()
        true
      case _ => err("Unable to set output state")
    }
  }

  command("getFilter") { ctx =>
    getCore(ctx).getFluidFilter map (x => CCResult(x.getName)) getOrElse CCResult.Null
  }

  command("setFilter") { ctx =>
    ctx.params(CCOption(CCString)) match {
      case Some(s) =>
        if (FluidRegistry.isFluidRegistered(s)) {
          getCore(ctx).setFluidFilter(FluidRegistry.getFluid(s))
          true
        } else {
          err("Unknown fluid")
        }
      case None =>
        getCore(ctx).clearFluidFilter()
        true
    }
  }
}

