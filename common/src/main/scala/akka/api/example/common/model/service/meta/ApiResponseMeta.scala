package akka.api.example.common.model.service.meta

import akka.api.example.common.model.service.ErrorMessage

/**
  * Created by bvalerio on 26/06/2018.
  */

/**
  * Meta data about the response that will contain status code and any error info if there was an error
  */
case class ApiResponseMeta(statusCode:Int, error:Option[ErrorMessage] = None)
