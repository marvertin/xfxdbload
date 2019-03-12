package cz.tconsult.lib.ifxdbload.workflow.executors;

import cz.tconsult.lib.ifxdbload.core.db.LoadContext;
import cz.tconsult.lib.ifxdbload.core.loaders.DbObjLoader;
import cz.tconsult.lib.ifxdbload.core.loaders.vue.VueLoader;

public class F240vueExecutor extends DbObjExecutor0 {


  @Override
  public DbObjLoader createDbObjLoader(final LoadContext ctx) {
    return new VueLoader(ctx);
  }


}
