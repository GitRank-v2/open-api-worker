package com.dragonguard.open_api.gitrepomember

import com.dragonguard.open_api.gitrepomember.dto.GitRepoInfoRequest
import com.dragonguard.open_api.gitrepomember.dto.GitRepoResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("git-repos")
class GitRepoController(
    private val gitRepoService: GitRepoService,
) {
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    fun getRepoInfo(@RequestBody request: GitRepoInfoRequest): GitRepoResponse =
        gitRepoService.getRepoInfo(request)
}
