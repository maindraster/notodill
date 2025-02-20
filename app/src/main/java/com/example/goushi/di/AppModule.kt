package com.example.goushi.di

import android.content.Context
import com.example.goushi.data.AppDatabase
import com.example.goushi.data.dao.BillDao
import com.example.goushi.data.dao.NoteDao
import com.example.goushi.data.dao.TodoDao
import com.example.goushi.data.repository.BillRepository
import com.example.goushi.data.repository.NoteRepository
import com.example.goushi.data.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideBillDao(database: AppDatabase): BillDao {
        return database.billDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepository(noteDao)
    }

    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }

    @Provides
    @Singleton
    fun provideBillRepository(billDao: BillDao): BillRepository {
        return BillRepository(billDao)
    }
}