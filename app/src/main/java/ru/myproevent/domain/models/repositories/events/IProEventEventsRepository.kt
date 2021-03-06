package ru.myproevent.domain.models.repositories.events

import io.reactivex.Completable
import io.reactivex.Single
import ru.myproevent.domain.models.entities.Event

interface IProEventEventsRepository {
    fun saveEvent(event: Event): Single<Event>
    fun editEvent(event: Event): Completable
    fun deleteEvent(event: Event): Completable
    fun deleteEvent(id: Long): Completable
    fun getEvent(id: Long): Single<Event>
    fun getEvents(): Single<List<Event>>
}

