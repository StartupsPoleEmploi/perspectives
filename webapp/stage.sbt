excludeFilter in digest := "*.scss"

pipelineStages := Seq(digest, gzip)