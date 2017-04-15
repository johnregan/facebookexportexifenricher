package com.johnregan.facebookexportexifenricher

import java.io.File

object CommonUtil {
  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)
}