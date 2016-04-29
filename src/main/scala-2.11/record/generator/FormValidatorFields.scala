package record.generator

class FormValidatorFields {

  case object StringField
  case class StringField(length:Int = 200)
  case class StringFieldMin(min: Int = 2, max: Int = 200)
  case object IntField
  case class IntFieldMin(min:Int = 100)
  case class IntFieldMax(max:Int = 100)
  case class IntFieldMinMax(min:Int = 100, max:Int = 100)

}
