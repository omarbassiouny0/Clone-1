package mega.privacy.android.app.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.app.domain.usecase.CheckNameCollision
import mega.privacy.android.app.domain.usecase.CreateShareKey
import mega.privacy.android.app.domain.usecase.GetChildrenNode
import mega.privacy.android.app.domain.usecase.GetNodeByHandle
import mega.privacy.android.app.namecollision.usecase.CheckNameCollisionUseCase
import mega.privacy.android.data.repository.MegaNodeRepository
import mega.privacy.android.domain.usecase.filenode.DefaultDeleteNodeVersionsByHandle
import mega.privacy.android.domain.usecase.filenode.DeleteNodeVersionsByHandle
import mega.privacy.android.domain.usecase.filenode.MoveNodeToRubbishByHandle

/**
 * Get node module
 *
 * Provides use cases for managing nodes
 */
@Module
@InstallIn(ViewModelComponent::class, SingletonComponent::class)
abstract class GetNodeModule {


    /**
     * Provides the [DeleteNodeVersionsByHandle] implementation
     *
     * @param defaultDeleteNodeVersionsByHandle the default implementation to be provided
     * @return [DeleteNodeVersionsByHandle]
     */
    @Binds
    abstract fun provideDeleteNodeVersionsByHandle(defaultDeleteNodeVersionsByHandle: DefaultDeleteNodeVersionsByHandle): DeleteNodeVersionsByHandle

    companion object {

        /**
         * Provides the [GetChildrenNode] implementation
         *
         * @param megaNodeRepository [MegaNodeRepository]
         * @return [GetChildrenNode]
         */
        @Provides
        fun provideGetChildrenNode(megaNodeRepository: MegaNodeRepository): GetChildrenNode =
            GetChildrenNode(megaNodeRepository::getChildrenNode)

        /**
         * Provides the [GetNodeByHandle] implementation
         *
         * @param megaNodeRepository [MegaNodeRepository]
         * @return [GetNodeByHandle]
         */
        @Provides
        fun provideGetNodeByHandle(megaNodeRepository: MegaNodeRepository): GetNodeByHandle =
            GetNodeByHandle(megaNodeRepository::getNodeByHandle)

        /**
         * Provides [CheckNameCollision] implementation
         * @param checkNameCollisionUseCase [CheckNameCollisionUseCase] is the current implementation, not following the current architecture
         * @return [CheckNameCollision]
         */
        @Provides
        fun provideCheckNameCollision(
            checkNameCollisionUseCase: CheckNameCollisionUseCase,
        ): CheckNameCollision =
            CheckNameCollision { nodeHandle, parentNodeHandle, type ->
                checkNameCollisionUseCase.check(
                    nodeHandle.longValue,
                    parentNodeHandle.longValue,
                    type,
                )
            }

        /**
         * Provides [MoveNodeToRubbishByHandle] implementation
         * @param megaNodeRepository [MegaNodeRepository]
         * @return [MoveNodeToRubbishByHandle]
         */
        @Provides
        fun provideMoveNodeToRubbishByHandle(
            megaNodeRepository: MegaNodeRepository,
        ): MoveNodeToRubbishByHandle =
            MoveNodeToRubbishByHandle(megaNodeRepository::moveNodeToRubbishBinByHandle)

        /**
         * Provides [CreateShareKey] implementation
         *
         * @param megaNodeRepository [MegaNodeRepository]
         * @return [CreateShareKey]
         */
        @Provides
        fun provideCreateShareKey(megaNodeRepository: MegaNodeRepository): CreateShareKey =
            CreateShareKey(megaNodeRepository::createShareKey)
    }
}
