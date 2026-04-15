package io.github.theflysong.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;

import io.github.theflysong.client.gl.shader.Shader;

/**
 * Resource Location，资源位置，表示一个资源在游戏中的唯一标识
 * 分为命名空间 + 类型 + 路径
 *
 * @author theflysong
 * @date 2026年4月14日
 */
public class ResourceLoader {
    @Nullable
    public static InputStream loadFile(String name) {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(name);
    }

    @Nullable
    public static InputStream loadFile(Identifier location) {
        return loadFile(location.toPath());
    }

    public static String loadText(Identifier location) {
        StringBuilder sb = new StringBuilder();
        InputStream file = Objects.requireNonNull(loadFile(location), "Couldn't load file from " + location);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file, StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                sb.append(line);
                while ((line = br.readLine()) != null) {
                    sb.append("\n").append(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load text from " + location, e);
        }
        return sb.toString();
    }

    public static ByteBuffer loadBinary(Identifier location) {
        try (InputStream stream = Objects.requireNonNull(ResourceLoader.loadFile(location), "Couldn't load file from " + location)) {
            byte[] bytes = stream.readAllBytes();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load binary from " + location, e);
        }
    }
}