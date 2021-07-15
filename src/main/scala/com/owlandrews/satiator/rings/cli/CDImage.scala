package com.owlandrews.satiator.rings.cli

import cats.effect.IO

import java.io.File
import java.nio.channels.Channels
import java.nio.file.{ Files, Path }
import scala.util.{ Failure, Success, Try }

case class CDImage(path: Path, ipBin: IpBin)

case class IpBin(
    hardwareId: String,
    makerId: String,
    productNumber: String,
    version: String,
    area: String
) {
  override def toString =
    s"Hardware Id: $hardwareId Maker Id: $makerId Product number: $productNumber Version: $version Area: $area"
}

trait CDImageOps {
  def open(file: File): Try[CDImage]
}

class CDImageBin extends CDImageOps {

  private val validHardwareId = "SEGA SEGASATURN"

  override def open(file: File): Try[CDImage] = {
    val path = file.toPath

    val load = Try(Files.newByteChannel(path)).map(Channels.newInputStream)

    val possibleCdImage = for {
      inputFile <- load
      imageData     = Iterator.continually(inputFile.read()).take(256).toArray
      hardwareId    = imageData.slice(16, 32).map(_.toChar).mkString.trim
      makerId       = imageData.slice(33, 47).map(_.toChar).mkString.trim
      productNumber = imageData.slice(48, 58).map(_.toChar).mkString.trim
      version       = imageData.slice(59, 64).map(_.toChar).mkString.trim
      area          = imageData.slice(80, 90).map(_.toChar).mkString.trim
      _             = inputFile.close()
    } yield CDImage(path, IpBin(hardwareId, makerId, productNumber, version, area))

    possibleCdImage.flatMap { in =>
      if (in.ipBin.hardwareId == validHardwareId) Success(in)
      else
        Failure(
          new IllegalArgumentException(
            s"Invalid sega saturn binary image, found incorrect hardware Id: ${in.ipBin.hardwareId}"
          )
        )
    }
  }
}

object CDImages {

  val universalAreaCode           = "JTUBKAEL"
  private val validCdImageFormats = List(".bin", ".img", ".iso")
  private val cdImageBin          = new CDImageBin

  def open(folder: String): IO[List[CDImage]] =
    for {
      _ <- IO(
        App.logger
          .info(s"Scanning for valid cd image files")
      )
      files <- IO(getFiles(new File(folder), List.empty[File]))
      _ <- IO(
        App.logger
          .info(s"Found ${files.length} images, loading IP.BIN...")
      )
    } yield files.par
      .collect {
        case bin if bin.getName.endsWith(".bin") =>
          (cdImageBin.open(bin) match {
            case s @ Success(_) => s
            case x @ Failure(error) =>
              App.logger.warn(s"Unable to open file ${bin.getName} ${error.getMessage}")
              x
          }).toOption
      }
      .toList
      .flatten

  def getFiles(dir: File, fileList: List[File]): List[File] = {
    val tmp = Option(dir.listFiles()).toList.flatten
    val res =
      for (f <- tmp)
        yield
          if (f.isDirectory)
            getFiles(f, fileList)
          else if (f.isFile && validCdImageFormats.exists(f.getName.endsWith)) {
            fileList ::: List(f)
          } else {
            fileList
          }
    res.flatten
  }
}
