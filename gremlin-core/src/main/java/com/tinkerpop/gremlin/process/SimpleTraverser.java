package com.tinkerpop.gremlin.process;


import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import com.tinkerpop.gremlin.structure.util.detached.DetachedProperty;
import com.tinkerpop.gremlin.structure.util.detached.DetachedVertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SimpleTraverser<T> implements Traverser<T>, Traverser.System<T> {

    private static final String PATH_ERROR_MESSAGE = "Path tracking is not supported by this Traverser: " + SimpleTraverser.class;

    protected T t;
    protected String future = NO_FUTURE;
    protected int loops = 0;
    protected Traversal.SideEffects sideEffects;

    protected SimpleTraverser() {

    }

    public SimpleTraverser(final T t, final Traversal.SideEffects sideEffects) {
        this.t = t;
        this.sideEffects = sideEffects;
    }

    @Override
    public T get() {
        return this.t;
    }

    @Override
    public Traversal.SideEffects getSideEffects() {
        return this.sideEffects;
    }

    @Override
    public void set(final T t) {
        this.t = t;
    }

    @Override
    public String getFuture() {
        return this.future;
    }

    @Override
    public void setFuture(final String label) {
        this.future = label;
    }

    @Override
    public Path getPath() {
        throw new IllegalStateException(PATH_ERROR_MESSAGE);
    }

    @Override
    public void setPath(final Path path) {
        throw new IllegalStateException(PATH_ERROR_MESSAGE);
    }

    @Override
    public int getLoops() {
        return this.loops;
    }

    @Override
    public void incrLoops() {
        this.loops++;
    }

    @Override
    public void resetLoops() {
        this.loops = 0;
    }

    @Override
    public <R> SimpleTraverser<R> makeChild(final String label, final R r) {
        final SimpleTraverser<R> traverser = new SimpleTraverser<>(r, this.sideEffects);
        traverser.future = this.future;
        traverser.loops = this.loops;
        return traverser;
    }

    @Override
    public SimpleTraverser<T> makeSibling() {
        final SimpleTraverser<T> traverser = new SimpleTraverser<>(this.t, this.sideEffects);
        traverser.future = this.future;
        traverser.loops = this.loops;
        return traverser;
    }

    @Override
    public void setSideEffects(final Traversal.SideEffects sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String toString() {
        return t.toString();
    }

    public int hashCode() {
        return this.t.hashCode() + this.getFuture().hashCode() + this.getLoops();
    }

    public boolean equals(final Object object) {
        return object instanceof SimpleTraverser && this.t.equals(((SimpleTraverser) object).get());
        /*return object instanceof SimpleTraverser &&
                ((SimpleTraverser) object).get().equals(this.t) &&
                ((SimpleTraverser) object).getFuture().equals(this.getFuture()) &&
                ((SimpleTraverser) object).getLoops() == this.getLoops();*/

    }

    @Override
    public SimpleTraverser<T> deflate() {
        if (this.t instanceof Vertex) {
            this.t = (T) DetachedVertex.detach((Vertex) this.t);
        } else if (this.t instanceof Edge) {
            this.t = (T) DetachedEdge.detach((Edge) this.t);
        } else if (this.t instanceof Property) {
            this.t = (T) DetachedProperty.detach((Property) this.t);
        }
        this.dropSideEffects();
        return this;
    }

    @Override
    public SimpleTraverser<T> inflate(final Vertex vertex, final Traversal traversal) {
        if (this.t instanceof DetachedVertex) {
            this.t = (T) ((DetachedVertex) this.t).attach(vertex);
        } else if (this.t instanceof DetachedEdge) {
            this.t = (T) ((DetachedEdge) this.t).attach(vertex);
        } else if (this.t instanceof DetachedProperty) {
            this.t = (T) ((DetachedProperty) this.t).attach(vertex);
        }
        this.sideEffects = traversal.sideEffects();
        return this;
    }
}
