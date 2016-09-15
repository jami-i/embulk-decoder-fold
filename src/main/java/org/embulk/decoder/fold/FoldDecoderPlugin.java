package org.embulk.decoder.fold;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigInject;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.BufferAllocator;
import org.embulk.spi.DecoderPlugin;
import org.embulk.spi.FileInput;
import org.embulk.spi.util.FileInputInputStream;
import org.embulk.spi.util.InputStreamFileInput;
import org.embulk.spi.util.Newline;

import java.io.IOException;
import java.io.InputStream;

public class FoldDecoderPlugin
        implements DecoderPlugin
{
    public interface PluginTask
            extends Task
    {
        @Config("fold_length")
        public int getFoldLength();

        // configuration option 2 (optional string, null is not allowed)
        @Config("newline")
        @ConfigDefault("\"\n\"")
        public Newline getNewLine();

        @ConfigInject
        public BufferAllocator getBufferAllocator();
    }

    @Override
    public void transaction(ConfigSource config, DecoderPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        control.run(task.dump());
    }

    @Override
    public FileInput open(TaskSource taskSource, FileInput fileInput)
    {
        final PluginTask task = taskSource.loadTask(PluginTask.class);

        final int foldLength = task.getFoldLength();
        final Newline newLine = task.getNewLine();

        final FileInputInputStream files = new FileInputInputStream(fileInput);

        return new InputStreamFileInput(
                task.getBufferAllocator(),
                new InputStreamFileInput.Provider() {
                    public InputStream openNext() throws IOException
                    {
                        if (!files.nextFile()) {
                            return null;
                        }
                        return newDecoderInputStream(files, foldLength, newLine);
                    }

                    public void close() throws IOException
                    {
                        files.close();
                    }
                });
    }

    private static InputStream newDecoderInputStream(InputStream file, int length, Newline newline) throws IOException
    {
        return new FoldInputStream(file, length, newline.getString());
    }
}
