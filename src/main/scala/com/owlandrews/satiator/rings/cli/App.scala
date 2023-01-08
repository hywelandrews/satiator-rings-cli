package com.owlandrews.satiator.rings.cli

import cats.effect.{ ExitCode, IO }
import cats.implicits._
import com.monovore.decline.effect.CommandIOApp
import com.monovore.decline.Opts
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

case class Targets(satiator: Option[String], covers: Option[String])

object App
    extends CommandIOApp(
      name = "satiator-rings-cli",
      header = "Setup the satiator rings menu from the command line"
    ) {

  private val satiatorPathOpts: Opts[Option[String]] =
    Opts
      .option[String](
        metavar = "path",
        long = "satiator",
        help = "volume containing iso's (bin/cue, img/cue, iso)"
      )
      .orNone

  private val coversPath: Opts[Option[String]] =
    Opts
      .option[String](
        metavar = "path",
        long = "covers",
        help = "folder containing cd front covers (jpg, jpeg, tga)"
      )
      .orNone

  private val uploadOpts: Opts[Targets] =
    Opts.subcommand("upload", "Adds boxart to satiator") {
      (satiatorPathOpts, coversPath).mapN(Targets)
    }

  val logger: Logger = Logger(LoggerFactory.getLogger(this.getClass))

  override def main: Opts[IO[ExitCode]] =
    uploadOpts.map { case Targets(Some(satiator), Some(covers)) =>
      // Open CD Image and read IP.BIN (only .bin / .cue I guess for now)
      // From IP.BIN get game id & region

      val loadCDsIO = CDImages.open(satiator)

      // Load all cover art from the provided folder
      val loadImagesIO = BoxArt.open(covers)

      (for {
        // Filter out anything that already has box art
        loadCDs    <- loadCDsIO.map(_.filter(cdImage => !BoxArt.exists(cdImage.path)))
        loadImages <- loadImagesIO
      } yield loadCDs.map { cdImage =>
        val maybeBoxart = loadImages.find { case (name, _) =>
          cdImage.ipBin.productNumber.nonEmpty && name.contains(cdImage.ipBin.productNumber)
        } orElse loadImages.find { case (name, _) =>
          // Fallback to removing the first party prefix - seems many US games have this in the image (i.e MK-12345)
          cdImage.ipBin.productNumber.nonEmpty && name.contains(
            cdImage.ipBin.productNumber.substring(3)
          )
        }

        val maybeValid = maybeBoxart.flatMap { case (name, image) =>
          // Resize based on region
          BoxArt.resize(image, cdImage.ipBin.area).map { sizedImage =>
            // If we do - convert to TGA
            // Upload to path (same as .cue / .bin)
            if (BoxArt.save(sizedImage, cdImage.path))
              cdImage.path.toString -> s"Successfully applied cover art using $name"
            else
              cdImage.path.toString -> "Unable to apply reformatting in TGA - contact satiator-rings-cli dev"
          }
        }

        // Boxart can either not be applied because:
        // 1) We did not find a matching image with its Product Number
        // 2) We have an invalid or unsupported area code

        val message = maybeValid.toLeft(
          if (maybeBoxart.isEmpty)
            cdImage.path.toString -> s"No box art could be found for product id ${cdImage.ipBin.productNumber}"
          else if (maybeValid.isEmpty)
            cdImage.path.toString -> s"Invalid area code  ${cdImage.ipBin.area} cannot apply image transformation"
          else
            cdImage.path.toString -> s"Unhandled error processing ${cdImage.ipBin.productNumber} - contact satiator-rings-cli dev"
        )

        message match {
          case Left(res) =>
            logger.info(s"${Console.GREEN}${res.productIterator.mkString(": ")}${Console.RESET}")
          case Right(error) =>
            logger.error(s"${Console.RED}${error.productIterator.mkString(": ")}${Console.RESET}")
        }

      }).as(ExitCode.Success)
    }
}
