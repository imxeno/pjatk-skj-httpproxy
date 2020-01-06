package pl.edu.pja.s19880.v2.headers;

import pl.edu.pja.s19880.v1.html.HTMLEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTTPHeaderMap implements Map<String, HTTPHeader> {
    HashMap<String, HTTPHeader> map = new HashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof String) {
            return map.containsKey(((String) key).toLowerCase());
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public HTTPHeader get(Object key) {
        if(key instanceof String) {
            return map.get(((String) key).toLowerCase());
        }
        return null;
    }

    public HTTPHeader put(HTTPHeader value) {
        return map.put(value.name().toLowerCase(), value);
    }

    @Override
    public HTTPHeader put(String key, HTTPHeader value) {
        return map.put(key.toLowerCase(), value);
    }

    @Override
    public HTTPHeader remove(Object key) {
        if(key instanceof String) {
            return map.remove(((String) key).toLowerCase());
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends HTTPHeader> m) {
        for(Entry<? extends String, ? extends HTTPHeader> e : m.entrySet()) {
            map.put(e.getKey().toLowerCase(), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<HTTPHeader> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, HTTPHeader>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(HTTPHeader header : values()) {
            sb.append(header).append(HTMLEntity.LINE_END);
        }
        return sb.toString();
    }
}
