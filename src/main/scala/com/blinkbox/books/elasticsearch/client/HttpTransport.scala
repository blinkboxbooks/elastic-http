package com.blinkbox.books.elasticsearch.client

import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction
import org.elasticsearch.action.admin.cluster.node.hotthreads.NodesHotThreadsAction
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction
import org.elasticsearch.action.admin.cluster.node.restart.NodesRestartAction
import org.elasticsearch.action.admin.cluster.node.shutdown.NodesShutdownAction
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsAction
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryAction
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesAction
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryAction
import org.elasticsearch.action.admin.cluster.repositories.verify.VerifyRepositoryAction
import org.elasticsearch.action.admin.cluster.reroute.ClusterRerouteAction
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsAction
import org.elasticsearch.action.admin.cluster.shards.ClusterSearchShardsAction
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotAction
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotAction
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsAction
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotAction
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusAction
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsAction
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksAction
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistAction
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesAction
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheAction
import org.elasticsearch.action.admin.indices.close.CloseIndexAction
import org.elasticsearch.action.admin.indices.create.CreateIndexAction
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsAction
import org.elasticsearch.action.admin.indices.flush.FlushAction
import org.elasticsearch.action.admin.indices.get.GetIndexAction
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingAction
import org.elasticsearch.action.admin.indices.mapping.get.{GetMappingsAction, GetFieldMappingsAction}
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction
import org.elasticsearch.action.admin.indices.open.OpenIndexAction
import org.elasticsearch.action.admin.indices.optimize.OptimizeAction
import org.elasticsearch.action.admin.indices.recovery.RecoveryAction
import org.elasticsearch.action.admin.indices.refresh.RefreshAction
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsAction
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsAction
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsAction
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction
import org.elasticsearch.action.admin.indices.status.IndicesStatusAction
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateAction
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesAction
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryAction
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerAction
import org.elasticsearch.action.admin.indices.warmer.get.GetWarmersAction
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerAction
import org.elasticsearch.action.bulk.BulkAction
import org.elasticsearch.action.count.CountAction
import org.elasticsearch.action.delete.DeleteAction
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction
import org.elasticsearch.action.exists.ExistsAction
import org.elasticsearch.action.explain.ExplainAction
import org.elasticsearch.action.get.{GetRequest, MultiGetAction, GetAction}
import org.elasticsearch.action.index.IndexAction
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptAction
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptAction
import org.elasticsearch.action.mlt.MoreLikeThisAction
import org.elasticsearch.action.percolate.{PercolateAction, MultiPercolateAction}
import org.elasticsearch.action.search._
import org.elasticsearch.action.suggest.SuggestAction
import org.elasticsearch.action.termvector.{TermVectorAction, MultiTermVectorsAction}
import org.elasticsearch.action.update.UpdateAction
import org.elasticsearch.action.{ActionResponse, ActionRequest, ActionFuture, Action}
import org.elasticsearch.client.{Client => ElasticClient}
import scala.language.existentials

trait HttpTransport {

  def doAction[Response <: ActionResponse, Client <: ElasticClient](
      action: Action[T, Response, _, Client] forSome { type T <: ActionRequest[T] }, request: ActionRequest[_]): ActionFuture[Response] = {

    action.name match {
      // Indices actions
      case AliasesExistAction.NAME => ???
      case AnalyzeAction.NAME => ???
      case ClearIndicesCacheAction.NAME => ???
      case CloseIndexAction.NAME => ???
      case CreateIndexAction.NAME => ???          // Needed
      case DeleteIndexAction.NAME => ???
      case DeleteIndexTemplateAction.NAME => ???
      case DeleteMappingAction.NAME => ???
      case DeleteWarmerAction.NAME => ???
      case FlushAction.NAME => ???                // Maybe
      case GetAliasesAction.NAME => ???
      case GetFieldMappingsAction.NAME => ???
      case GetIndexAction.NAME => ???
      case GetIndexTemplatesAction.NAME => ???
      case GetMappingsAction.NAME => ???
      case GetSettingsAction.NAME => ???
      case GetWarmersAction.NAME => ???
      case IndicesAliasesAction.NAME => ???
      case IndicesExistsAction.NAME => ???
      case IndicesSegmentsAction.NAME => ???
      case IndicesStatsAction.NAME => ???
      case IndicesStatusAction.NAME => ???
      case OpenIndexAction.NAME => ???
      case OptimizeAction.NAME => ???
      case PutIndexTemplateAction.NAME => ???
      case PutMappingAction.NAME => ???           // Needed
      case PutWarmerAction.NAME => ???
      case RecoveryAction.NAME => ???
      case RefreshAction.NAME => ???
      case TypesExistsAction.NAME => ???
      case UpdateSettingsAction.NAME => ???
      case ValidateQueryAction.NAME => ???

      // Cluster actions
      case ClusterHealthAction.NAME => ???
      case ClusterRerouteAction.NAME => ???
      case ClusterSearchShardsAction.NAME => ???
      case ClusterStateAction.NAME => ???
      case ClusterStatsAction.NAME => ???
      case ClusterUpdateSettingsAction.NAME => ???
      case CreateSnapshotAction.NAME => ???
      case DeleteRepositoryAction.NAME => ???
      case DeleteSnapshotAction.NAME => ???
      case GetRepositoriesAction.NAME => ???
      case NodesHotThreadsAction.NAME => ???
      case NodesInfoAction.NAME => ???
      case NodesRestartAction.NAME => ???
      case NodesShutdownAction.NAME => ???
      case NodesStatsAction.NAME => ???
      case PendingClusterTasksAction.NAME => ???
      case PutRepositoryAction.NAME => ???
      case RestoreSnapshotAction.NAME => ???
      case SnapshotsStatusAction.NAME => ???
      case VerifyRepositoryAction.NAME => ???

      // Client actions
      case BulkAction.NAME => ???                 // Needed
      case ClearScrollAction.NAME => ???
      case CountAction.NAME => ???                // Maybe
      case DeleteAction.NAME => ???               // Maybe
      case DeleteByQueryAction.NAME => ???        // Maybe
      case ExistsAction.NAME => ???
      case ExplainAction.NAME => ???
      case GetAction.NAME =>  ???                 // Needed
      case GetIndexedScriptAction.NAME => ???
      case GetSnapshotsAction.NAME => ???
      case IndexAction.NAME => ???                // Needed
      case MoreLikeThisAction.NAME => ???         // Needed
      case MultiGetAction.NAME => ???             // Needed
      case MultiPercolateAction.NAME => ???
      case MultiSearchAction.NAME => ???          // Maybe
      case MultiTermVectorsAction.NAME => ???
      case PercolateAction.NAME => ???
      case PutIndexedScriptAction.NAME => ???
      case SearchAction.NAME => ???               // Needed
      case SearchScrollAction.NAME => ???
      case SuggestAction.NAME => ???              // Maybe
      case TermVectorAction.NAME => ???
      case UpdateAction.NAME => ???               // Needed
    }
  }
}


