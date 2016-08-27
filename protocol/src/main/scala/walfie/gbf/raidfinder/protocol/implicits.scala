package walfie.gbf.raidfinder.protocol

/** Convenience classes for dealing with Protobuf `oneof` messages */
package object implicits {
  implicit class RequestMessageOps(val message: RequestMessage) extends AnyVal {
    import RequestMessage.Data._

    // Could also do `raidBossesMessage orElse subscriptionChangeMessage`
    // but it doesn't check for exhaustive matches, so we'll use this verbose way
    def toRequest(): Option[Request] = message.data match {
      case RaidBossesMessage(v) => Some(v)
      case SubscribeMessage(v) => Some(v)
      case UnsubscribeMessage(v) => Some(v)
      case Empty => None
    }
  }

  implicit class RequestOps(val request: Request) extends AnyVal {
    import RequestMessage.Data._

    def toMessage(): RequestMessage = {
      val data = request match {
        case v: RaidBossesRequest => RaidBossesMessage(v)
        case v: SubscribeRequest => SubscribeMessage(v)
        case v: UnsubscribeRequest => UnsubscribeMessage(v)
      }
      RequestMessage(data = data)
    }
  }

  implicit class ResponseMessageOps(val message: ResponseMessage) extends AnyVal {
    import ResponseMessage.Data._

    def toResponse(): Option[Response] = message.data match {
      case RaidTweetMessage(v) => Some(v)
      case RaidBossesMessage(v) => Some(v)
      case SubscriptionStatusMessage(v) => Some(v)
      case ErrorMessage(v) => Some(v)
      case Empty => None
    }
  }

  implicit class ResponseOps(val response: Response) extends AnyVal {
    import ResponseMessage.Data._

    def toMessage(): ResponseMessage = {
      val data = response match {
        case v: RaidTweetResponse => RaidTweetMessage(v)
        case v: RaidBossesResponse => RaidBossesMessage(v)
        case v: SubscriptionStatusResponse => SubscriptionStatusMessage(v)
        case v: ErrorResponse => ErrorMessage(v)
      }
      ResponseMessage(data = data)
    }
  }
}

