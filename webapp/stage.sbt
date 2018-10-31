import sbt.SimpleFileFilter

excludeFilter in digest := "*.scss"

includeFilter in uglify := new SimpleFileFilter(f => f.getAbsolutePath.contains(".js") && !f.getAbsolutePath.contains("vendor"))

pipelineStages := Seq(uglify, digest, gzip)