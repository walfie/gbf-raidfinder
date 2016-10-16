package walfie.gbf.raidfinder.protocol

/** Convenience classes for dealing with Protobuf `oneof` messages */
package object syntax {
  implicit class RequestMessageOps(val message: RequestMessage) extends AnyVal {
    import RequestMessage.Data._

    // Could also do `raidBossesMessage orElse followMessage orElse ...`
    // but it doesn't check for exhaustive matches, so we'll use this verbose way
    def toRequest(): Option[Request] = message.data match {
      case AllRaidBossesMessage(v) => Some(v)
      case RaidBossesMessage(v) => Some(v)
      case FollowMessage(v) => Some(v)
      case UnfollowMessage(v) => Some(v)
      case Empty => None
    }
  }

  implicit class RequestOps(val request: Request) extends AnyVal {
    import RequestMessage.Data._

    def toMessage(): RequestMessage = {
      val data = request match {
        case v: AllRaidBossesRequest => AllRaidBossesMessage(v)
        case v: RaidBossesRequest => RaidBossesMessage(v)
        case v: FollowRequest => FollowMessage(v)
        case v: UnfollowRequest => UnfollowMessage(v)
      }
      RequestMessage(data = data)
    }
  }

  implicit class ResponseMessageOps(val message: ResponseMessage) extends AnyVal {
    import ResponseMessage.Data._

    def toResponse(): Option[Response] = message.data match {
      case RaidTweetMessage(v) => Some(v)
      case RaidBossesMessage(v) => Some(v)
      case FollowStatusMessage(v) => Some(v)
      case WelcomeMessage(v) => Some(v)
      case KeepAliveMessage(v) => Some(v)
      case Empty => None
    }
  }

  implicit class ResponseOps(val response: Response) extends AnyVal {
    import ResponseMessage.Data._

    def toMessage(): ResponseMessage = {
      val data = response match {
        case v: RaidTweetResponse => RaidTweetMessage(v)
        case v: RaidBossesResponse => RaidBossesMessage(v)
        case v: FollowStatusResponse => FollowStatusMessage(v)
        case v: WelcomeResponse => WelcomeMessage(v)
        case v: KeepAliveResponse => KeepAliveMessage(v)
      }
      ResponseMessage(data = data)
    }
  }
}

