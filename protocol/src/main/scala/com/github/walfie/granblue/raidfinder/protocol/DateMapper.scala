package com.github.walfie.granblue.raidfinder.protocol

import java.util.Date
import com.trueaccord.scalapb.TypeMapper

object DateMapper {
  implicit val typeMapper = TypeMapper(new Date(_: Long))(_.getTime)
}

