package com.owlandrews.satiator.rings.cli

import cats.effect.IO

import java.io.{ File, IOException }
import java.nio.channels.Channels
import java.nio.file.{ Files, Path }
import scala.util.Try

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
  def open(file: File): CDImage
}

class CDImageBin extends CDImageOps {
  override def open(file: File): CDImage = {
    val path = file.toPath

    val load = Try(Files.newByteChannel(path))

    val mightBeLoaded = load
      .getOrElse(throw new IOException(s"Unable to load file: $path"))

    val in = Channels.newInputStream(mightBeLoaded)

    val imageData = Iterator
      .continually(in.read())
      .take(256) // do whatever you want with each byte
      .toArray

    val hardwareId    = imageData.slice(16, 31).map(_.toChar).mkString.trim
    val makerId       = imageData.slice(32, 47).map(_.toChar).mkString.trim
    val productNumber = imageData.slice(48, 58).map(_.toChar).mkString.trim
    val version       = imageData.slice(59, 64).map(_.toChar).mkString.trim
    val area          = imageData.slice(80, 90).map(_.toChar).mkString.trim

    mightBeLoaded.close()

    CDImage(path, IpBin(hardwareId, makerId, productNumber, version, area))
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
    } yield files.par.collect {
      case bin if bin.getName.endsWith(".bin") => cdImageBin.open(bin)
    }.toList

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
