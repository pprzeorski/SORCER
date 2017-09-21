package sorcer.core.context.model.ent;

import sorcer.core.context.ContextSelection;
import sorcer.core.context.ServiceContext;
import sorcer.service.*;
import sorcer.service.modeling.Duo;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Valuation;
import sorcer.service.modeling.ent;
import sorcer.util.bdb.objects.UuidObject;
import sorcer.util.url.sos.SdbUtil;

import java.net.URL;
import java.rmi.RemoteException;

public class Entry<V> extends Association<String, V> implements Identifiable, Callable<V>, Setter, ent<V> {

    protected boolean negative;

    // its arguments is persisted
    protected boolean isPersistent = false;

    protected ContextSelection contextSelector;

    public Entry() {
    }

    public Entry(String key) {
        this.key = key;
    }

    public Entry(String key, V item) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        this.key = key;
        this.item = item;

        if (sorcer.util.url.sos.SdbUtil.isSosURL(item)) {
            isPersistent = true;
        }
        if (item != null && item.getClass().getName().indexOf("Lambda") > 0) {
            type = Functionality.Type.LAMBDA;
        }
    }

    @Override
    public void setValue(Object value) throws SetterException, RemoteException {
        item = (V) value;
    }

    public void setContextSelector(ContextSelection contextSelector) {
        this.contextSelector = contextSelector;
    }

    public V getData(Arg... args) {
        Object data = item;
        if (item instanceof Entry && ((Entry)data).getKey().equals(key)) {
            return (V) ((Entry)item).getData();
        }
        return (V) data;
    }

    @Override
    public V get(Arg... args) throws ContextException {
        V val = item;
        Object out = null;
        URL url = null;
        try {
            substitute(args);
            if (isPersistent) {
                if (SdbUtil.isSosURL(val)) {
                    out = (V) ((URL) val).getContent();
                    if (out instanceof UuidObject)
                        val = (V) ((UuidObject) val).getObject();
                } else {
                    if (val instanceof UuidObject) {
                        url = SdbUtil.store(val);
                    } else {
                        UuidObject uo = new UuidObject(val);
                        uo.setName(key);
                        url = SdbUtil.store(uo);
                    }
                    item = (V)url;
                }
            } else if (val instanceof Invocation) {
                Context cxt = (Context) Arg.getServiceModel(args);
                val = (V) ((Invocation) val).invoke(cxt, args);
            } else if (val instanceof Evaluation) {
                if (val instanceof Entry && ((Entry)val).getName().equals(key)) {
                    val = (V) ((Entry)val).get(args);
                } else {
                    val = ((Evaluation<V>) val).getValue(args);
                }
            } else if (val instanceof Valuation) {
                val = (V) ((Valuation) val).value();
            } else if (val instanceof ServiceFidelity) {
                // return the selected fidelity of this entry
                for (Arg arg : args) {
                    if (arg instanceof Fidelity) {
                        if (((Fidelity)arg).getPath().equals(key)) {
                            ((ServiceFidelity)val).setSelect(arg.getName());
                            break;
                        }
                    }
                }
                val = (V) ((Entry)((ServiceFidelity) val).getSelect()).get(args);
            } else if (val instanceof Callable) {
                val = (V) ((Callable)val).call(args);
            } else if (val instanceof Service) {
                out = (V) ((Service)val).execute(args);
            }
            out = val;
        } catch (Exception e) {
            throw new ContextException(e);
        }
        if (contextSelector != null && out instanceof Context) {
            try {
                out = (V) contextSelector.doSelect(val);
            } catch (ContextException e) {
                throw new ContextException(e);
            }
        }
        if (out instanceof Number && negative) {
            Number result = (Number) val;
            Double rd = result.doubleValue() * -1;
            out = (V) rd;
        }
        return (V) out;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }


    @Override
    public Entry act(Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(key, result);
        }
    }

    @Override
    public Duo act(String entryName, Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(entryName, result);
        }
    }

    public Object execute(Arg... args) throws ServiceException, RemoteException {
        Domain cxt = Arg.getServiceModel(args);
        if (cxt != null) {
            // entry substitution
            ((ServiceContext)cxt).putValue(key, item);
            return cxt;
        } else {
            return item;
        }
    }

    @Override
    public V call(Arg... args) throws EvaluationException, RemoteException {
        try {
            return get(args);
        } catch (ContextException e) {
            throw new EvaluationException(e);
        }
    }

    public void substitute(Arg... entries) throws SetterException {
        if (entries != null) {
            for (Arg a : entries) {
                if (a instanceof ContextSelection) {
                    setContextSelector((ContextSelection) a);
                }
            }
        }
    }
}
