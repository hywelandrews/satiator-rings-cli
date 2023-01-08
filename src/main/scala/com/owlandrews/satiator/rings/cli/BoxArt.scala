package com.owlandrews.satiator.rings.cli

import cats.effect.IO
import cats.implicits._
import com.twelvemonkeys.image.ResampleOp

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

trait BoxArt {
  def open(file: File): Image
}

class BoxArtDefault extends BoxArt {
  override def open(file: File): Image = {
    import javax.imageio.ImageIO
    ImageIO.read(file)
  }
}

object BoxArt {

  private val validImageFormats = List(".jpg", ".jpeg", ".png", ".tga")
  private val boxArtDefault     = new BoxArtDefault
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
      case jpg if jpg.getName.endsWith(".jpg")    => jpg.getName  -> boxArtDefault.open(jpg)
      case jpeg if jpeg.getName.endsWith(".jpeg") => jpeg.getName -> boxArtDefault.open(jpeg)
      case png if png.getName.endsWith(".png")    => png.getName  -> boxArtDefault.open(png)
    }.toMap

  def exists(path: Path): Boolean = binPathToBoxFile(path).exists()

  def resize(image: Image, region: String): Option[Image] =
    region match {
      case "J"                                              => scaleImage(image, 80, 80).some
      case "U" | "T"                                        => scaleImage(image, 64, 100).some
      case _ if CDImages.universalAreaCode.contains(region) =>
        // Todo: we should re validate area code somehow (is that possible?), assume Japanese cover is multi region
        scaleImage(image, 80, 80).some
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
    try ImageIO.write(bImage, "TGA", binPathToBoxFile(path))
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

  private def scaleImage(image: Image, width: Int, height: Int): Image = {
    val resizeOp = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS)
    resizeOp.filter(image.asInstanceOf[BufferedImage], null)
  }

  private def binPathToBoxFile(p: Path) = new File(p.getParent.toFile, "BOX.TGA")
}
