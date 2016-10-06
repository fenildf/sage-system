function comments_render(el, sourceType, sourceId) {
  $(el).html($('#tmpl-comment-list').html())
  return new Vue({
    el: el,
    data: {
      sourceType: sourceType,
      sourceId: sourceId,
      comments: [],
      visible: true,
      editorContent: '',
      commentBeingReplied: null
    },

    ready: function () {
      this.$emit('fetch')
    },

    events: {
      fetch: function () {
        var self = this
        $.get('/api/comments', {sourceType: this.sourceType, sourceId: this.sourceId})
          .done(function (comments) {
            self.comments = comments
            self.alert('alert-info', '评论加载成功')
          })
          .fail(function (msg) {
            self.alert('alert-danger', '评论加载失败: ' + msg)
          })
      }
    },

    methods: {
      postComment: function (alsoForward) {
        var self = this
        if (!self.editorContent || self.editorContent.trim() == '') {
          self.alert('alert-danger', '请输入内容')
          return
        }
        self.showAlert('alert-warning', '正在发送...')
        $.post('/api/comments/new', {
          content: this.editorContent,
          sourceType: this.sourceType,
          sourceId: this.sourceId,
          forward: alsoForward
        }).done(function () {
          self.editorContent = ''
          self.alert('alert-success', '发送成功')
        }).fail(function () {
          self.alert('alert-danger', '发送失败')
        })
      },

      postReply: function (c, alsoForward) {
        var self = this
        if (!c.replyEditorContent || c.replyEditorContent.trim() == '') {
          self.alert('alert-danger', '请输入内容')
          return
        }
        self.showAlert('alert-warning', '正在发送...')
        $.post('/api/comments/new', {
          content: c.replyEditorContent,
          sourceType: this.sourceType,
          sourceId: this.sourceId,
          replyUserId: c.authorId,
          forward: alsoForward
        }).done(function () {
          c.replyEditorContent = ''
          self.commentBeingReplied = null
          self.alert('alert-success', '发送成功')
        }).fail(function () {
          self.alert('alert-danger', '发送失败')
        })
      },

      toggleWriteReply: function (c) {
        if (this.commentBeingReplied != c) {
          this.commentBeingReplied = c
        } else {
          this.commentBeingReplied = null
        }
      },

      showAlert: function (cls, msg) {
        return $(this.$el).find('.action-alert').attr('class', 'action-alert ' + cls).text(msg).stop().css('opacity', '1').show()
      },

      alert: function (cls, msg) {
        return this.showAlert(cls, msg).fadeOut(2000)
      },
    }
  })
}

function toggleTweetComments(){
  var $tweet = $(this).parents('.tweet')
  var tweetId = $tweet.attr('tweet-id')
  var $commentsContainer = $tweet.find('.comments-container')
  var vm = $commentsContainer.data('vm')
  if (vm) {
    if (vm.$data.visible) {
      vm.$data.visible = false
    } else {
      vm.$data.visible = true
      vm.$emit('fetch')
    }
  } else {
    vm = comments_render($tweet.find('.comments-container')[0], 2, tweetId)
    $commentsContainer.data('vm', vm)
  }
}