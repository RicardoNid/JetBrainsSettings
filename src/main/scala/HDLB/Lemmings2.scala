package HDLB

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

class Lemmings2 extends Component {
  val io = new Bundle {
    val clk = in Bool
    val areset = in Bool
    val bump_left = in Bool
    val bump_right = in Bool
    val ground = in Bool
    val walk_left = out Bool
    val walk_right = out Bool
    val aaah = out Bool
  }

  object walkingType extends SpinalEnum {
    val walkingS, walkRightS, fallingS = newElement()
  }

  object directionType extends SpinalEnum {
    val leftS, rightS = newElement()
  }

  import walkingType._
  import directionType._

  val clockConfig = ClockDomainConfig(resetKind = ASYNC) // design : 设置同步/异步reset
  new ClockingArea(new ClockDomain(clock = io.clk, reset = io.areset, config = clockConfig)) {
    // design : 设计代码
    val walkingStateNext = walkingType()
    val walkingState = RegNext(walkingStateNext) init (walkingS)

    walkingStateNext := walkingState
    switch(walkingState) {
      is(walkingS) {
        when(!io.ground) {
          walkingStateNext := fallingS
        }
      }
      is(fallingS) {
        when(io.ground) {
          walkingStateNext := walkingS
        }
      }
    }

    val directionStateNext = directionType()
    val directionState = RegNext(directionStateNext) init (leftS)

    directionStateNext := directionState
    switch(directionState) {
      is(leftS) {
        when(io.ground && walkingState === walkingS && io.bump_left) {
          directionStateNext := rightS
        }
      }
      is(rightS) {
        when(io.ground && walkingState === walkingS && io.bump_right) {
          directionStateNext := leftS
        }
      }
    }

    io.walk_left := (walkingState === walkingS && directionState === leftS)
    io.walk_right := (walkingState === walkingS && directionState === rightS)
    io.aaah := (walkingState === fallingS)
  }
  noIoPrefix()
}

object Lemmings2 {
  def main(args: Array[String]): Unit = {
    SpinalConfig(mode = SystemVerilog).generate(new Lemmings2().setDefinitionName("top_module"))
  }
}
