Embulk::JavaPlugin.register_decoder(
  "fold", "org.embulk.decoder.fold.FoldDecoderPlugin",
  File.expand_path('../../../../classpath', __FILE__))
