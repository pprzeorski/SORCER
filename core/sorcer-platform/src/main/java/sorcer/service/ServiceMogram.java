package sorcer.service;

import net.jini.config.*;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.co.tuple.ExecPath;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ContextSelector;
import sorcer.core.context.model.ent.Coupling;
import sorcer.core.context.model.ent.Entry;
import sorcer.core.context.model.ent.MdaEntry;
import sorcer.core.monitor.MonitoringSession;
import sorcer.core.plexus.FidelityManager;
import sorcer.core.plexus.MorphFidelity;
import sorcer.core.provider.Provider;
import sorcer.core.provider.ServiceBean;
import sorcer.core.provider.ServiceProvider;
import sorcer.core.service.Projection;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.modeling.Data;
import sorcer.service.modeling.Functionality;
import sorcer.service.modeling.Model;
import sorcer.util.GenericUtil;
import sorcer.util.Pool;
import sorcer.util.Pools;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.*;

/**
 * Created by sobolemw on 5/4/15.
 */
public abstract class ServiceMogram extends MultiFiSlot<String, Object> implements Mogram, Activity, ServiceBean, Exec, Serializable, SorcerConstants {

    protected final static Logger logger = LoggerFactory.getLogger(ServiceMogram.class.getName());

    static final long serialVersionUID = 1L;

    protected Uuid mogramId;

    protected Uuid parentId;

    protected Mogram parent;

    protected String parentPath = "";

    protected ExecPath execPath;

    protected Uuid sessionId;

    protected String subjectId;

    protected Subject subject;

    protected String ownerId;

    protected String runtimeId;

    protected Long lsbId;

    protected Long msbId;

    protected String domainId;

    protected String subdomainId;

    protected String domainName;

    protected String subdomainName;

    protected FidelityManagement fiManager;

    protected Projection projection;

    // list of fidelities of this mogram
    protected String[] profile;

    protected MogramStrategy mogramStrategy;

    protected Differentiator differentiator;

    protected Fidelity<MdaEntry> mdaFi;

    protected List<Coupling> couplings;

    protected ContextSelector contextSelector;

    /**
     * execution status: INITIAL|DONE|RUNNING|SUSPENDED|HALTED
     */
    protected Integer status = Exec.INITIAL;

    protected Integer priority;

    protected String description;

    protected String projectName;

    protected boolean isRevaluable = false;

    // indicates that is the parent of another mogram
    protected boolean isSuper = false;

    // true if the exertion has to be initialized (to original state)
    // or used as is after resuming from suspension or failure
    protected boolean isInitializable = true;

    protected String dbUrl;

    protected MetaFi multiMetaFi = new Metafidelity();

    protected MorphFidelity serviceMorphFidelity;

    protected SorcerPrincipal principal;

    // the current fidelity alias, as it is named in 'fidelities'
    // its original name might be different if aliasing is used
    // for already existing names
    protected String serviceFidelitySelector;

    // Date of creation of this Exertion
    protected Date creationDate = new Date();

    protected Date lastUpdateDate;

    protected Date goodUntilDate;

    protected String accessClass;

    protected Boolean isExportControlled;

    protected static String defaultName = "mogram-";

    public static boolean debug = false;

    // sequence number for unnamed mogram instances
    protected static int count = 0;

    protected MonitoringSession monitorSession;

    protected Signature builder;

    protected String configFilename;

    protected transient Provider provider;

    protected boolean isEvaluated = false;

    protected ServiceMogram() {
        this(defaultName + count++);
    }

    public ServiceMogram(String name) {
        if (name == null || name.length() == 0)
            this.key = defaultName + count++;
        else
            this.key = name;
        init();
    }

    public ServiceMogram(String name, Signature builder) {
        this(name);
        this.builder = builder;
    }

    protected void init() {
        mogramId = UuidFactory.generate();
        multiFi = new ServiceFidelity();
        domainId = "0";
        subdomainId = "0";
        accessClass = PUBLIC;
        isExportControlled = Boolean.FALSE;
        status = new Integer(INITIAL);
        principal = new SorcerPrincipal(System.getProperty("user.name"));
        principal.setId(principal.getName());
        setSubject(principal);
    }

    @Override
    public void setName(String name) {
        key = name;
    }

    public Uuid getMogramId() {
        return mogramId;
    }

    @Override
    public void setParentId(Uuid parentId) {
        this.parentId = parentId;
    }

    public Uuid getParentId() {
        return parentId;
    }

    public List<Mogram> getAllMograms() {
        List<Mogram> exs = new ArrayList<Mogram>();
        getMograms(exs);
        return exs;
    }

    public List<Mogram> getMograms(List<Mogram> exs) {
        exs.add(this);
        return exs;
    }

    public List<String> getAllMogramIds() {
        List<String> mogIdsList = new ArrayList<String>();
        for (Mogram mo : getAllMograms()) {
            mogIdsList.add(mo.getId().toString());
        }
        return mogIdsList;
    }

    public void trimAllNotSerializableSignatures() throws SignatureException {
        trimNotSerializableSignatures();
        for (Mogram m : getAllMograms()) {
            ((ServiceMogram) m).trimNotSerializableSignatures();
        }
    }

    public Mogram getMogram(String componentMogramName) {
        if (key.equals(componentMogramName)) {
            return this;
        } else {
            List<Mogram> mograms = getAllMograms();
            for (Mogram m : mograms) {
                if (m.getName().equals(componentMogramName)) {
                    return m;
                }
            }
            return null;
        }
    }

    public void setService(Service provider) {
        NetSignature ps = (NetSignature) getProcessSignature();
        ps.setProvider(provider);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int value) {
        status = value;
    }

    @Override
    public Uuid getId() {
        return mogramId;
    }

    public void setId(Uuid id) {
        mogramId = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String id) {
        runtimeId = id;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setSubdomainId(String subdomaindId) {
        this.subdomainId = subdomaindId;
    }

    public String getSubdomainId() {
        return subdomainId;
    }

    public Uuid getSessionId() {
        return sessionId;
    }

    public void setSessionId(Uuid sessionId) {
        this.sessionId = sessionId;
    }

    public Mogram getParent() {
        return parent;
    }

    public void setParent(Mogram parent) {
        this.parent = parent;
    }

    public SorcerPrincipal getPrincipal() {
        return principal;
    }

    public void setPrincipal(SorcerPrincipal principal) {
        this.principal = principal;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public Functionality.Type getType() {
        return Functionality.Type.MOGRAM;
    }

    private void setSubject(Principal principal) {
        if (principal == null)
            return;
        Set<Principal> principals = new HashSet<Principal>();
        principals.add(principal);
        subject = new Subject(true, principals, new HashSet(), new HashSet());
    }

    public SorcerPrincipal getSorcerPrincipal() {
        if (subject == null)
            return null;
        Set<Principal> principals = subject.getPrincipals();
        Iterator<Principal> iterator = principals.iterator();
        while (iterator.hasNext()) {
            Principal p = iterator.next();
            if (p instanceof SorcerPrincipal)
                return (SorcerPrincipal) p;
        }
        return null;
    }

    public String getPrincipalId() {
        SorcerPrincipal p = getSorcerPrincipal();
        if (p != null)
            return getSorcerPrincipal().getId();
        else
            return null;
    }

    public void setPrincipalId(String id) {
        SorcerPrincipal p = getSorcerPrincipal();
        if (p != null)
            p.setId(id);
    }

    public long getMsbId() {
        return (msbId == null) ? -1 : msbId.longValue();
    }

    public void setLsbId(long leastSig) {
        if (leastSig != -1) {
            lsbId = new Long(leastSig);
        }
    }

    public void setMsbId(long mostSig) {
        if (mostSig != -1) {
            msbId = new Long(mostSig);
        }
    }

    public void setPriority(int p) {
        priority = p;
    }

    public int getPriority() {
        return (priority == null) ? MIN_PRIORITY : priority;
    }

    public Signature getProcessSignature() {
        ServiceFidelity selectedFi = (ServiceFidelity)multiFi.getSelect();
        if (selectedFi != null  && selectedFi.getSelect() != null) {
            return (Signature)selectedFi.getSelect();
        } else {
            if (selectedFi == null) {
                return null;
            }
        }

        Signature sig = null;
        for (Object s : selectedFi.selects) {
            if (s instanceof Signature && ((Signature)s).getType() == Signature.Type.PROC) {
                sig = (Signature)s;
                break;
            }
        }
        if (sig != null) {
            // a select is just a compute signature for the selection
            selectedFi.select = sig;
        }
        return sig;
    }

    public void trimNotSerializableSignatures() throws SignatureException {
        if (multiFi != null) {
            for (Object fi : multiFi.getSelects()) {
                if (fi instanceof ServiceFidelity)
                    trimNotSerializableSignatures((Fidelity) fi);
            }
        }
    }

    private void trimNotSerializableSignatures(Fidelity<Signature> fidelity) throws SignatureException {
        if (fidelity.getSelect() instanceof Signature) {
            Iterator<Signature> i = fidelity.getSelects().iterator();
            while (i.hasNext()) {
                Signature sig = i.next();
                Class prvType = sig.getServiceType();
                if (!prvType.isInterface()
                    && !Serializable.class.isAssignableFrom(prvType)) {
                    i.remove();
                    if (sig == fidelity.getSelect()) {
                        fidelity.setSelect((Signature) null);
                    }
                    logger.warn("removed not serializable signature for: {}", prvType);
                }
            }
        }
    }

    public List<Signature> getApdProcessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getType() == Signature.Type.APD_DATA)
                sl.add((Signature)s);
        }
        return sl;
    }

    public List<Signature> getPreprocessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getType() == Signature.Type.PRE)
                sl.add((Signature)s);
        }
        return sl;
    }

    public List<Signature> getPostprocessSignatures() {
        List<Signature> sl = new ArrayList<Signature>();
        for (Object s : ((ServiceFidelity)multiFi.getSelect()).getSelects()) {
            if (s instanceof Signature && ((Signature)s).getType() == Signature.Type.POST)
                sl.add((Signature)s);
        }
        return sl;
    }

    /**
     * Adds a new signature <code>signature</code> for this mogram fidelity.
     **/
    public void addSignature(Signature... signatures) {
        if (signatures == null)
            return;
        String id = getOwnerId();
        if (id == null) {
            id = System.getProperty("user.name");
        }
        for (Signature sig : signatures) {
            ((ServiceSignature) sig).setOwnerId(id);
        }
        ServiceFidelity sFi = (ServiceFidelity) multiFi.getSelect();
        if (sFi == null) {
            multiFi.setSelect(new ServiceFidelity());
            sFi = (ServiceFidelity) multiFi.getSelect();
        }
        for (Signature sig : signatures) {
            sFi.getSelects().add(sig);
        }
    }

    /**
     * Removes a signature <code>signature</code> for this exertion.
     *
     * @see #addSignature
     */
    public void removeSignature(Signature signature) {
        ((ServiceFidelity)multiFi.getSelect()).getSelects().remove(signature);
    }

    public void setAccessClass(String s) {
        if (SENSITIVE.equals(s) || CONFIDENTIAL.equals(s) || SECRET.equals(s))
            accessClass = s;
        else
            accessClass = PUBLIC;
    }

    public String getAccessClass() {
        return (accessClass == null) ? PUBLIC : accessClass;
    }

    public void isExportControlled(boolean b) {
        isExportControlled = new Boolean(b);
    }

    public boolean isExportControlled() {
        return isExportControlled.booleanValue();
    }

    public Date getGoodUntilDate() {
        return goodUntilDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setGoodUntilDate(Date date) {
        goodUntilDate = date;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String id) {
        ownerId = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getSubdomainName() {
        return subdomainName;
    }

    public void setSubdomainName(String subdomainName) {
        this.subdomainName = subdomainName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String path) {
        parentPath = path;
    }

    public boolean isInitializable() {
        return isInitializable;
    }

    public void setIsInitializable(boolean isInitializable) {
        this.isInitializable = isInitializable;
    }

    public Mogram setExecPath(ExecPath execPath)
            throws ContextException {
        this.execPath = execPath;
        return this;
    }

    public ExecPath getExecPath() {
        return execPath;
    }

    public boolean isSuper() {
        return isSuper;
    }

    public void setSuper(boolean aSuper) {
        isSuper = aSuper;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public Fidelity getSelectedFidelity() {
        return (Fidelity) multiFi.getSelect();
    }

    public ContextSelector getContextSelector() {
        return contextSelector;
    }

    public void setContextSelector(ContextSelector contextSelector) {
        this.contextSelector = contextSelector;
    }

    public Mogram getComponentMogram(String path) {
        return this;
    }

    abstract public Mogram clearScope() throws MogramException;

    @Override
    public void applyFidelity(String name) {
        // implement in subclasses
    }

    /**
     * <p>
     * Returns <code>true</code> if this context is for modeling, otherwise
     * <code>false</code>. If context is for modeling then the values of this
     * context that implement the {@link Evaluation} interface are evaluated for
     * its requested evaluated values.
     * </p>
     *
     * @return the <code>true</code> if this context is revaluable.
     */
    public boolean isModeling() {
        return isRevaluable;
    }

    /*public boolean setValid() {
        return setValid;
    }

    public void setValid(boolean state) {
        setValid = state;
    }*/

    public void setModeling(boolean isRevaluable) {
        this.isRevaluable = isRevaluable;
    }

    public String toString() {
        StringBuffer info = new StringBuffer()
                .append(this.getClass().getName()).append(": " + key);
        info.append("\n  status=").append(status);
        info.append(", mogram ID=").append(mogramId);
        return info.toString();
    }

    /**
     * <p>
     * Returns the monitor session of this exertion.
     * </p>
     *
     * @return the monitorSession
     */
    public MonitoringSession getMonitorSession() {
        return monitorSession;
    }

    /**
     * <p>
     * Assigns a monitor session for this mograms.
     * </p>
     *
     * @param monitorSession the monitorSession to set
     */
    public void setMonitorSession(MonitoringSession monitorSession) {
        this.monitorSession = monitorSession;
    }

    public MorphFidelity getServiceMorphFidelity() {
        return serviceMorphFidelity;
    }

    public void setServiceMorphFidelity(MorphFidelity morphFidelity) {
        this.serviceMorphFidelity = morphFidelity;
    }

    @Override
    public Signature getBuilder(Arg... args)  {
        return builder;
    }

    /**
     * Initialization by a service provider (container)
     * when this mogram is used as as a service bean.
     */
    public void init(Provider provider) {
        this.provider = provider;
        logger.info("*** provider init properties:\n"
                + GenericUtil.getPropertiesString(((ServiceProvider)provider).getProviderProperties()));
        System.getProperties().putAll(((ServiceProvider)provider).getProviderProperties());
    }

    public void setBuilder(Signature builder) {
        this.builder = builder;
    }

    public void setSelectedFidelity(ServiceFidelity fidelity) {
        this.multiFi.setSelect(fidelity);
    }

    public MetaFi getMultiMetaFi() {
        return multiMetaFi;
    }

    public void setMultiMetaFi(MetaFi multiMetaFi) {
        this.multiMetaFi = multiMetaFi;
    }

    public FidelityManagement getFidelityManager() {
        return fiManager;
    }

    public FidelityManagement getRemoteFidelityManager() throws RemoteException {
        return getFidelityManager();
    }

    public void setFidelityManager(FidelityManagement fiManager) {
        this.fiManager = fiManager;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    public String[] getProfile() {
        return profile;
    }

    public void setProfile(String[] profile) {
        this.profile = profile;
    }

    public Fidelity selectFidelity(Arg... entries) {
        Fidelity fi = null;
        if (entries != null && entries.length > 0) {
            for (Arg a : entries)
                if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.SELECT) {
                    fi = (Fidelity) selectFidelity(a.getName());
                } else if (a instanceof Fidelity && ((Fidelity) a).fiType == Fidelity.Type.COMPONENT) {
                    fi = selectComponentFidelity((Fidelity) a);
                } else if (a instanceof ServiceFidelity && ((ServiceFidelity) a).fiType == ServiceFidelity.Type.META) {
                    fi = selectCompositeFidelity((ServiceFidelity) a);
                }
        }
        return fi;
    }

    public Fidelity selectFidelity(String selector) {
        multiFi.selectSelect(selector);
        return (Fidelity) multiFi.getSelect();
    }

    public Fidelity selectComponentFidelity(Fidelity componentFidelity) {
        Mogram ext = getComponentMogram(componentFidelity.getPath());
        String fn = componentFidelity.getName();
        Fidelity cf = (Fidelity) ((ServiceFidelity)ext.getMultiFi()).getSelect(fn);
        if (cf == null) {
            logger.warn("no such fidelity for {}" + componentFidelity);
        }
        return cf;
    }

    public Fidelity selectCompositeFidelity(Fidelity fidelity) {
        if (fidelity.fiType == Fi.Type.META) {
            for (Object obj : fidelity.selects) {
                if (obj instanceof ServiceFidelity) {
                    if (((ServiceFidelity) obj).fiType == ServiceFidelity.Type.COMPONENT)
                        selectComponentFidelity((ServiceFidelity) obj);
                    else
                        selectFidelity(((Fidelity) obj).getName());
                }
            }
        }
        return fidelity;
    }

    @Override
    public void reconfigure(Fidelity... fidelities) throws ContextException, RemoteException {
        if (fiManager != null) {
            if (fidelities.length == 1 && fidelities[0] instanceof ServiceFidelity) {
                List<Service> fiList = ((ServiceFidelity) fidelities[0]).getSelects();
                Fidelity[] fiArray = new Fidelity[fiList.size()];
                fiList.toArray(fiArray);
                fiManager.reconfigure(fiArray);
            }
            fiManager.reconfigure(fidelities);
        }
    }

    @Override
    public void morph(String... metaFiNames) throws ContextException, RemoteException {
        if (fiManager != null) {
            fiManager.morph(metaFiNames);
            profile = metaFiNames;
        } else {
            throw new ContextException("No fiManager available in " + this.getClass().getName());
        }
    }

    @Override
    public MogramStrategy getMogramStrategy() {
        return mogramStrategy;
    }

    public void setModelStrategy(MogramStrategy strategy) {
        mogramStrategy = strategy;
    }

    public boolean isBatch() {
        return ((ServiceFidelity)multiFi.getSelect()).getSelects().size() > 1;
    }

    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public void loadFiPool() {
        if (configFilename == null) {
            logger.warn("No mogram configuration file available for: {}", key);
        } else {
            initConfig(new String[]{configFilename});
        }
    }

    public void initConfig(String[] args) {
        Configuration config;
        try {
            config = ConfigurationProvider.getInstance(args, getClass()
                    .getClassLoader());

            Pool[] pools = (Pool[]) config.getEntry(Pools.COMPONENT, Pools.FI_POOL, Pool[].class);
            Pool<Fidelity, Service> pool = new Pool<>();
            pool.setFiType(Fi.Type.VAR_FI);
            for (int i = 0; i < pools.length; i++) {
                pool.putAll((Map<? extends Fidelity, ? extends ServiceFidelity>) pools[i]);
            }
            Pools.putFiPool(this, pool);

            List[] projections = (List[]) config.getEntry(Pools.COMPONENT, Pools.FI_PROJECTIONS, List[].class);
            Map<String, ServiceFidelity> metafidelities =
                    ((FidelityManager) getFidelityManager()).getMetafidelities();
            for (int i = 0; i < projections.length; i++) {
                for (Projection po : (List<Projection>) projections[i]) {
                    metafidelities.put(po.getName(), po);
                }
            }
        } catch (net.jini.config.ConfigurationException e) {
            logger.warn("configuratin failed for: " + configFilename);
            e.printStackTrace();
        }
        logger.debug("config fiPool: " + Pools.getFiPool(mogramId));
    }

    public <T> T getInstance() throws SignatureException {
        if (builder != null) {
            ServiceMogram mogram = (ServiceMogram) sorcer.co.operator.instance(builder);
            Class<T> clazz;
            clazz = (Class<T>) mogram.getClass();
            return (T) clazz.cast(mogram);
        } else {
            throw new SignatureException("No mogram builder available");
        }
    }

    public List<Coupling> getCouplings() {
        return couplings;
    }

    public void setCouplings(List<Coupling> couplings) {
        this.couplings = couplings;
    }

    public Fidelity<MdaEntry> setMdaFi(Context context) throws ContextException {
       if(mdaFi == null) {
           Object mdaComponent = context.get(Context.MDA_PATH);
           if (mdaComponent != null) {
               if (mdaComponent instanceof MdaEntry) {
                   mdaFi = new Fidelity(((MdaEntry)mdaComponent).getName());
                   mdaFi.addSelect((MdaEntry) mdaComponent);
                   mdaFi.setSelect((MdaEntry)mdaComponent);
               } else if (mdaComponent instanceof ServiceFidelity
                       && ((ServiceFidelity) mdaComponent).getFiType().equals(Fi.Type.MDA)) {
                   mdaFi = (Fidelity) mdaComponent;
               }
           }
       }
       return mdaFi;
    }

    public Fidelity<MdaEntry> getMdaFi() {
        return mdaFi;
    }

    @Override
    public String getProjectionFi(String projectionName) throws ContextException, RemoteException {
        return ((FidelityManager)fiManager).getProjectionFi(projectionName);
    }

    public Differentiator getDifferentiator() {
        return differentiator;
    }

    public void setDifferentiator(Differentiator mogramDifferentiator) {
        this.differentiator = mogramDifferentiator;
    }

    @Override
    public Mogram deploy(List<Signature> builders) throws ConfigurationException {
        // to be implemented in subclasses
        return this;
    }

    @Override
    public void update(Setup... contextEntries) throws ContextException, RemoteException {
        // implement in subclasses
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
    public Data act(String entryName, Arg... args) throws ServiceException, RemoteException {
        Object result = this.execute(args);
        if (result instanceof Entry) {
            return (Entry)result;
        } else {
            return new Entry(entryName, result);
        }
    }

    @Override
    public void reportException(String message, Throwable t) {
        mogramStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, ProviderInfo info) {
        // reimplement in sublasses
        mogramStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, Provider provider) {
        // reimplement in sublasses
        mogramStrategy.addException(t);
    }

    @Override
    public void reportException(String message, Throwable t, Provider provider, ProviderInfo info) {
        // reimplement in sublasses
        mogramStrategy.addException(t);
    }

    public String getServiceFidelitySelector() {
        return serviceFidelitySelector;
    }

    public void setServiceFidelitySelector(String serviceFidelitySelector) {
        this.serviceFidelitySelector = serviceFidelitySelector;
    }

    @Override
    public Object getEvaluatedValue(String path) throws ContextException {
        // reimplement in subclasses
        if (isEvaluated) {
            if (this instanceof Context) {
                try {
                    if (this instanceof Model) {
                        return ((Context)((Model) this).getResult()).getValue(path);
                    } else {
                        return ((Context) this).getValue(path);
                    }
                } catch (RemoteException e) {
                    throw new ContextException(e);
                }
            } else if (this instanceof Exertion) {
                ((Exertion) this).getValue(path);
            }
        }
        throw new ContextException(getName() + "mogram not evaluated yet");
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public void setEvaluated(boolean evaluated) {
        isEvaluated = evaluated;
    }

    public Mogram clear() throws MogramException {
        mogramStrategy.getOutcome().clear();
        isValid = false;
        isChanged = true;
        clearScope();
        return this;
    }
}
