package com.mobile.requests;

import com.mobile.model.Eid;

/**
 * Created by ivonneortega on 3/5/18.
 */

public class OpenAppEventRequest {
    String ct, ci, e, c, m, mc, u, o, ak, l, ln;
    Eid eid;

    public OpenAppEventRequest(String ct, String ci, String e, String c, String m, String mc, String u, String o, String ak, String l, String ln, Eid eid) {
        this.ct = ct;
        this.ci = ci;
        this.e = e;
        this.c = c;
        this.m = m;
        this.mc = mc;
        this.u = u;
        this.o = o;
        this.ak = ak;
        this.l = l;
        this.ln = ln;
        this.eid = eid;
    }
}
