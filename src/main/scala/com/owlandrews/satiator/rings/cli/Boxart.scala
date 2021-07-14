package com.owlandrews.satiator.rings.cli

import cats.effect.IO
import cats.implicits._

import java.awt.Image
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

trait Boxart {
  def open(file: File): Image
}

class BoxartDefault extends Boxart {
  override def open(file: File): Image = {
    import javax.imageio.ImageIO
    ImageIO.read(file)
  }
}

object Boxart {

  private val validImageFormats = List(".jpg", ".jpeg", ".png", ".tga")
  private val boxartDefault     = new BoxartDefault

  def open(folder: String): IO[Map[String, Image]] =
    for {
      _ <- IO(
        App.logger
          .info(s"Scanning for valid cover images")
      )
      files <- IO(getFiles(new File(folder), List.empty[File]))
      _ <- IO(
        App.logger
          .info(s"Found ${files.length} covers, loading in formats JPG / JPEG / PNG")
      )
    } yield files.collect {
      case jpg if jpg.getName.endsWith(".jpg")    => jpg.getName  -> boxartDefault.open(jpg)
      case jpeg if jpeg.getName.endsWith(".jpeg") => jpeg.getName -> boxartDefault.open(jpeg)
      case png if png.getName.endsWith(".png")    => png.getName  -> boxartDefault.open(png)
    }.toMap

  def resize(image: Image, region: String): Option[Image] =
    region match {
      case "J"                                                      => image.getScaledInstance(80, 80, Image.SCALE_DEFAULT).some
      case "U" | "T"                                                => image.getScaledInstance(64, 100, Image.SCALE_DEFAULT).some
      case universal if CDImages.universalAreaCode.contains(region) =>
        // Todo: we should re validate area code somehow (is that possible?), assume Japanese cover is multi region
        image.getScaledInstance(80, 80, Image.SCALE_DEFAULT).some
      case _ => Option.empty[Image]
    }

  def save(image: Image, path: Path): Boolean = {
    import java.awt.image.BufferedImage
    // Create a buffered image with transparency
    val bImage =
      new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)

    // Draw the image on to the buffered image
    val bGr = bImage.createGraphics
    bGr.drawImage(image, 0, 0, null)
    bGr.dispose()

    // Return the buffered image in TGA format
    try ImageIO.write(bImage, "TGA", new File(path.getParent.toFile, "BOX.TGA"))
    catch {
      case e: Throwable => println(e); false
    }
  }

  def getFiles(dir: File, fileList: List[File]): List[File] = {
    val tmp = dir.listFiles().toList
    val res =
      for (f <- tmp)
        yield
          if (f.isDirectory)
            getFiles(f, fileList)
          else if (f.isFile && validImageFormats.exists(f.getName.endsWith)) {
            fileList ::: List(f)
          } else {
            fileList
          }
    res.flatten
  }
}
