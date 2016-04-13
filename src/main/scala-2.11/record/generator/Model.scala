package record.generator

import LiftMongoMeta._

case class Person(name:StringField = 200, age: IntField)
case class Car(nrOfWheels: IntField, make:StringField = 100)
case class Text( minMax : MinMaxField = (10, 100) )



