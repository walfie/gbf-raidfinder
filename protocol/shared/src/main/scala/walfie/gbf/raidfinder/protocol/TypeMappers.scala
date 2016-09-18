package walfie.gbf.raidfinder.protocol

import com.trueaccord.scalapb.TypeMapper
import java.util.Date

object TypeMappers {
  implicit val dateMapper = TypeMapper(new Date(_: Long))(_.getTime)
}

