package com.github.walfie.granblue.raidfinder.protocol

import java.util.Date
import com.trueaccord.scalapb.TypeMapper

object TypeMappers {
  implicit val dateMapper = TypeMapper(new Date(_: Long))(_.getTime)
}

