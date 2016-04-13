package record.generator

object LiftMongoMeta {

  type StringField = Int
  type IntField = Int
  type MinField = Int
  type MaxField = Int
  type MinMaxField = (MinField, MaxField)

  trait BsonMetaRecord
  trait MongoRecord


}
