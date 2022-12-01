package com.grup.repositories.abstract

import com.grup.models.Group
import com.grup.interfaces.IGroupRepository
import com.grup.other.Id
import com.grup.other.idSerialName
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal abstract class RealmGroupRepository : IGroupRepository {
    protected abstract val realm: Realm

    override fun createGroup(group: Group): Group? {
        return realm.writeBlocking {
            copyToRealm(group)
        }
    }

    override fun findGroupById(groupId: Id): Group? {
        return realm.query<Group>("$idSerialName == $0", groupId).first().find()
    }

    override fun findAllGroupsAsFlow(): Flow<List<Group>> {
        return realm.query<Group>().find().asFlow().map { changes ->
            changes.list.toList()
        }
    }

    override fun updateGroup(group: Group, block: (Group) -> Unit): Group? {
        return realm.writeBlocking {
            findLatest(group)!!.apply {
                block(this)
            }
        }
    }

    override fun close() {
        realm.close()
    }
}
