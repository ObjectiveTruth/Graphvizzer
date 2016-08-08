package filters

import javax.inject.Inject
import filters.AccessLogAndTimerFilter.AccessLogAndTimerFilter
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

class ProdFilter @Inject()(
                              gzip: GzipFilter,
                              log: AccessLogAndTimerFilter

                          ) extends DefaultHttpFilters(gzip, log) { }
