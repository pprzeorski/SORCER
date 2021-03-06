package sorcer.service;

import net.jini.lease.LeaseRenewalManager;
import sorcer.co.tuple.ExecDependency;
import sorcer.core.DispatchResult;
import sorcer.core.Dispatcher;
import sorcer.core.context.ServiceContext;
import sorcer.service.modeling.Discipline;
import sorcer.service.modeling.Exploration;
import sorcer.service.modeling.ExploreException;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import static sorcer.co.operator.path;

public class DisciplineExplorer implements Service, Exploration {

    Multidiscipline transdiscipline;
    // exec discipline dependencies
    protected Map<String, List<ExecDependency>> dependentDisciplines;

    public Map<String, List<ExecDependency>> getDependentDisciplines() {
        return dependentDisciplines;
    }

    public void setDependentDisciplines(Map<String, List<ExecDependency>> dependentDisciplines) {
        this.dependentDisciplines = dependentDisciplines;
    }

    @Override
    public Object execute(Arg... args) throws ServiceException {
        try {
            List<Fidelity> fis = Arg.selectFidelities(args);
            if (fis != null && fis.size() > 0) {
                transdiscipline.selectFi(fis.get(0));
            }
            Exertion xrt = transdiscipline.getDispatcher();
            if (transdiscipline.input != null) {
                if (transdiscipline.inConnector != null) {
                    xrt.setContext(((ServiceContext) transdiscipline.input).updateContextWith(transdiscipline.inConnector));
                } else {
                    xrt.setContext(transdiscipline.input);
                }
            }
            xrt.dispatch(transdiscipline.getGovernance());
            transdiscipline.result = xrt.exert();
            execDependencies(transdiscipline.getName(), args);
            return transdiscipline.getOutput();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    public void execDependencies(String path, Arg... args) throws ContextException {
        Map<String, List<ExecDependency>> dpm = dependentDisciplines;
        if (dpm != null && dpm.get(path) != null) {
            List<ExecDependency> del = dpm.get(path);
            Discipline dis = transdiscipline.getDiscipline(path);
            if (del != null && del.size() > 0) {
                for (ExecDependency de : del) {
                    List<Path> dpl = (List<Path>) de.getImpl();
                    if (dpl != null && dpl.size() > 0) {
                        for (Path p : dpl) {
                            try {
                                transdiscipline.getDiscipline(p.path).execute(args);
                            } catch (ServiceException e) {
                                e.printStackTrace();
                            } catch (RemoteException e) {
                                throw new ContextException(e);
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public Context explore(Context searchContext, Arg... args) throws ExploreException, RemoteException {
        return null;
    }
}
