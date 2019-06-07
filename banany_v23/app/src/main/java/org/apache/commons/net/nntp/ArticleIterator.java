package org.apache.commons.net.nntp;

import java.util.Iterator;

class ArticleIterator implements Iterator<Article>, Iterable<Article> {
    private final Iterator<String> stringIterator;

    public ArticleIterator(Iterable<String> iterableString) {
        this.stringIterator = iterableString.iterator();
    }

    public boolean hasNext() {
        return this.stringIterator.hasNext();
    }

    public Article next() {
        return NNTPClient.__parseArticleEntry((String) this.stringIterator.next());
    }

    public void remove() {
        this.stringIterator.remove();
    }

    public Iterator<Article> iterator() {
        return this;
    }
}
