package akka.api.example.common.model

/**
  * Trait to mix into case classes that represent lightweight representations of the fields for
  * an entity modeled as an actor
  */
trait EntityFieldsObject[FO] {
  /**
    * Assigns an id to the fields object, returning a new instance
    *
    * @param id The id to assign
    */
  def assignId(id: Int): FO

  def id: Int

  def deleted: Boolean

  def markDeleted: FO
}
