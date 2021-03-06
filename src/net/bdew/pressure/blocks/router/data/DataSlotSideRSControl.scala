/*
 * Copyright (c) bdew, 2013 - 2015
 * https://github.com/bdew/pressure
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.pressure.blocks.router.data

import net.bdew.lib.data.base.DataSlotContainer
import net.bdew.lib.multiblock.data.RSMode

case class DataSlotSideRSControl(name: String, parent: DataSlotContainer) extends DataSlotDirectionMap(RSMode, RSMode.ALWAYS)
