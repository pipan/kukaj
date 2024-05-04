package gaspapp.kukaj

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import gaspapp.kukaj.source.kukaj.KukajListLoader
import gaspapp.kukaj.source.kukaj.KukajSource
import gaspapp.kukaj.source.steppelife.SteppelifeSource

class Services {
    companion object {
        private lateinit var httpQueue: RequestQueue
        private lateinit var sourceService: SourceService
        private lateinit var listLoader: KukajListLoader

        fun init(context: Context) {
            this.httpQueue = Volley.newRequestQueue(context)
            this.listLoader = KukajListLoader(Repository.getLiveStreamStore(), this.httpQueue)

            val kukajSource: KukajSource = KukajSource(Repository.getLiveStreamStore(), this.httpQueue)
            val steppelifeSource: SteppelifeSource = SteppelifeSource(Repository.getLiveStreamStore(), this.httpQueue)

            this.sourceService = SourceService(arrayOf(kukajSource, steppelifeSource).toList())
        }

        fun getHttpQueue(): RequestQueue {
            return this.httpQueue
        }

        fun getSourceService(): SourceService {
            return this.sourceService
        }

        fun getListLoader(): KukajListLoader {
            return this.listLoader
        }
    }
}