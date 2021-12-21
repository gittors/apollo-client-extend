package com.gittors.apollo.extend.chain.context;

/**
 * @author zlliu
 * @date 2020/8/14 22:30
 */
public class Context {
    /**
     * Context name.
     */
    private final String name;

    /**
     * The origin of this context
     */
    private String origin = "";

    public Context(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "Context{" +
                "name='" + name + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }
}
