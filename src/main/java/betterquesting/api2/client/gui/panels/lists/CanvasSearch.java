package betterquesting.api2.client.gui.panels.lists;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import betterquesting.api2.client.gui.misc.IGuiRect;

public abstract class CanvasSearch<T, E> extends CanvasScrollingBuffered {

    private String searchTerm = "";
    private Iterator<E> searching = null;
    private final Stopwatch searchTime = Stopwatch.createStarted();
    private int resultWidth = 256; // Used for organising ongoing search results even if the size changes midway
    private int searchIdx = 0; // Where are we in the ongoing search?
    private final ArrayDeque<T> pendingResults = new ArrayDeque<>();
    private final List<T> savedResults = new ArrayList<>();

    public CanvasSearch(IGuiRect rect) {
        super(rect);
    }

    public void setSearchFilter(String text) {
        this.searchTerm = text.toLowerCase();
        refreshSearch();
    }

    @Override
    public void initPanel() {
        super.initPanel();
        refreshSearch();
    }

    @Override
    public void drawPanel(int mx, int my, float partialTick) {
        updateSearch();
        updateResults();

        super.drawPanel(mx, my, partialTick);
    }

    public boolean isSearching() { return searching != null || !pendingResults.isEmpty(); }

    public void refreshSearch() {
        this.resetCanvas();
        this.searchIdx = 0;
        this.searching = getIterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
        this.savedResults.clear();
    }

    private void updateSearch() {
        if (searching == null) {
            return;
        } else if (!searching.hasNext()) {
            searching = null;
            return;
        }

        searchTime.reset().start();

        ArrayDeque<T> tmp = new ArrayDeque<>();
        while (searching.hasNext() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 10) {
            E entry = searching.next();

            if (entry != null) {
                queryMatches(entry, searchTerm, tmp);
            }
        }

        pendingResults.addAll(tmp);
        savedResults.addAll(tmp);

        searchTime.stop();

        if (!searching.hasNext())
            searching = null;

    }

    private void updateResults() {
        if (pendingResults.isEmpty()) {
            return;
        }

        searchTime.reset().start();

        int count = 0;
        while (!pendingResults.isEmpty() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 10 && count < 200) {
            if (addResult(pendingResults.poll(), searchIdx, resultWidth)) {
                searchIdx++;
                count++;
            }
        }

        searchTime.stop();
        flushBuffer();
    }

    public List<T> getResults() { return Collections.unmodifiableList(savedResults); }

    protected abstract Iterator<E> getIterator();

    protected abstract void queryMatches(E value, String query, final ArrayDeque<T> results);

    protected abstract boolean addResult(T entry, int index, int cachedWidth);

}
