import enumeratum.{Enum, EnumEntry}

package object common {

  sealed trait MarketSegmentEnum extends EnumEntry
  case object MarketSegmentEnum extends Enum[MarketSegmentEnum] {
    case object Electricity extends MarketSegmentEnum
    case object Gas extends MarketSegmentEnum
    val values = findValues
  }
}
