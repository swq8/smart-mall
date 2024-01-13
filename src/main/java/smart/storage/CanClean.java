package smart.storage;

/**
 * 判断指定的文件能否被清除
 */
@FunctionalInterface
public interface CanClean {
    boolean canClean(String path);
}
