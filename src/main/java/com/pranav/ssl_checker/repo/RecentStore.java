package com.pranav.ssl_checker.repo;
import com.pranav.ssl_checker.service.TlsProbe;

//        * simple memory store that keeps the last 10 assessment results.
//        * Key is hostname. New items push out the oldest item. with arrays and loops.

public class RecentStore {

    private static final int Limit = 10;

    //parallel arrays keep it easy to see
    private final String[] names = new String[Limit];
    private final TlsProbe.Result[] results = new TlsProbe.Result[Limit];
    private int size = 0 ;  // how many are filled


    // synchronized means only one thread can use this method at a time.
    // In Spring Boot, many requests can run in parallel, so this prevents
    // two threads from changing the shared arrays at the same time.
    // It’s safe to use here because the work is small and quick.

    public synchronized void put(TlsProbe.Result r) {   //    add or replaced by hostname
        String host = r.getHostname();
        int idx = indexOf(host);
        if (idx >= 0) {
            // already present, just update the result
            names[idx] = host;
            results[idx] = r;
            return; // avoid falling through and duplicating the entry
        }
        if (size < Limit) {
            names[size] = host;
            results[size] = r;
            size++;
        } else {
            //    remove the oldest at  position 0 by shifting left
            for (int i = 1; i < Limit; i++) {
                names[i - 1] = names[i];
                results[i - 1] = results[i];
            }
            //put the new one at the end
            names[Limit - 1] = host;
            results[Limit - 1] = r;
        }
    }

    //    Get by hostname. Returns null if not found.
    public synchronized TlsProbe.Result get(String host) {
        int idx = indexOf(host);
        return idx >= 0 ? results[idx] : null;   //condition ? value_if_true : value_if_false
    }

    //Delete one hostname if present.
    // Why: user clicked Delete, and we keep at most 10 results in fixed arrays.
    public synchronized void delete(String host) {
        //find index; if missing, return; shift items left to fill the gap; decrement size; clear the now-unused last slot.
        int idx = indexOf(host);
        if (idx < 0) return;
        for (int i = idx + 1; i < size; i++) {
            names[i - 1] = names[i];
            results[i - 1] = results[i];
        }
        size--;
        names[size] = null;
        results[size] = null;
    }

    //remove all
    public synchronized void clear() {
        for (int i = 0; i < size; i++) {
            names[i] = null;
            results[i] = null;
        }
        size = 0;
    }

//            * WHY:
//            *  We need to show the “most recent 10” assessments on the UI with the newest on top.
//            *  Internally we append new results at the end of a fixed-size array (oldest at index 0, newest at index size-1).
//            *  Returning a reversed COPY keeps callers from touching our internal arrays and gives them exactly what they need.
//
//            * HOW:
//            *  1) Allocate a new array sized to the number of valid items (size) so there are no nulls.
//            *  2) Fill it by reading from the end of the internal array toward the start (reverse order).
//            *  3) Because the method is synchronized, no one else can modify the store while we copy, so the snapshot is consistent.

    //    Return a copy of results newest first.
    public synchronized TlsProbe.Result[] listNewestFirst() {
        TlsProbe.Result[] out = new TlsProbe.Result[size];
        for (int i = 0; i < size; i++) {
            out[i] = results[size - 1 - i];
        }
        return out;
    }

    //    Find array index of hostname, or -1 when not found.
    private int indexOf(String host) {
        for (int i = 0; i < size; i++) {
            if (host.equalsIgnoreCase(names[i])) return i;
        }
        return -1;
    }
}
