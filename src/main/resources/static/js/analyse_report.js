$(function() {

	$.ajax({
		url : "getChartData",
		data : {
			'gitUrl' : $("#git_url").text()
		},
		dataType : "json",
		async : true,
		type : "GET",

		success : function(data) {
			$('#chart').highcharts({
				credits : {
					enabled : false
				},
				title : {
					text : '',
					x : -20
				// center
				},
				xAxis : {
					categories : data['commits']
				},
				yAxis : {
					title : {
						text : ''
					},
					plotLines : [ {
						value : 0,
						width : 1,
						color : '#808080'
					} ]
				},
				legend : {
					layout : 'vertical',
					align : 'right',
					verticalAlign : 'middle',
					borderWidth : 0
				},
				series : [ {
					name : '代码行数',
					data : data['totalLineCount']
				}, {
					name : '注释行数',
					data : data['commentLineCount']
				}, {
					name : '成员变量数',
					data : data['fieldCount']
				}, {
					name : '方法数',
					data : data['methodCount']
				}, {
					name : '最大圈复杂度',
					data : data['maxCoc']
				} ]
			});

		}
	});

	hljs.initHighlightingOnLoad();

	$.ajaxSetup({
		cache : false
	});

	var commits = [];
	var activeIndex;
	var pastIndex;

	var fileNodeId;
	$('#tree')
			.jstree(
					{
						'core' : {
							'state' : {
								"opened" : true
							},
							'data' : {
								'url' : 'getFileNode.do',
								'data' : function(node) {
									return {
										'id' : node.id,
										'gitUrl' : $("#git_url").text()
									};
								}
							},
							'check_callback' : function(o, n, p, i, m) {
								if (m && m.dnd && m.pos !== 'i') {
									return false;
								}
								return true;
							},
							'force_text' : true,
							'themes' : {
								'responsive' : false,
								'variant' : 'small',
								'stripes' : true
							}
						},
						'sort' : function(a, b) {
							return this.get_type(a) === this.get_type(b) ? (this
									.get_text(a) > this.get_text(b) ? 1 : -1)
									: (this.get_type(a) >= this.get_type(b) ? 1
											: -1);
						},
						'contextmenu' : {},
						'types' : {
							'default' : {
								'icon' : 'folder'
							},
							'file' : {
								'valid_children' : [],
								'icon' : 'file'
							}
						},
						'unique' : {
							'duplicate' : function(name, counter) {
								return name + ' ' + counter;
							}
						},
						'plugins' : [ 'state', 'dnd', 'sort', 'types', 'unique' ]
					})
			.on(
					'changed.jstree',
					function(e, data) {
						if (data && data.selected && data.selected.length) {
							$
									.get(
											'getFileContent.do?id='
													+ data.selected.join(':')+'&gitUrl='+$("#git_url").text(),
											function(d) {
												if (d
														&& typeof d.type !== 'undefined') {
													$('#data .content').hide();
													switch (d.type) {
													case 'text':
													case 'txt':
													case 'md':
													case 'htaccess':
													case 'log':
													case 'sql':
													case 'php':
													case 'js':
													case 'json':
													case 'css':
													case 'html':
													case 'java':

														$('#data .code').show();
														$('#code').html(
																d.content);
														$('pre code')
																.each(
																		function(
																				i,
																				block) {
																			hljs
																					.highlightBlock(block);
																		});
														if (d.type === 'java') {

															fileNodeId = data.selected
																	.join(':');
															$(".slider")
																	.slider(
																			"value",
																			activeIndex)
																	.slider(
																			"pips",
																			"refresh");
															showCommitSlider(fileNodeId);
														}
														break;
													case 'png':
													case 'jpg':
													case 'jpeg':
													case 'bmp':
													case 'gif':
														$('#data .image img')
																.one(
																		'load',
																		function() {
																			$(
																					this)
																					.css(
																							{
																								'marginTop' : '-'
																										+ $(
																												this)
																												.height()
																										/ 2
																										+ 'px',
																								'marginLeft' : '-'
																										+ $(
																												this)
																												.width()
																										/ 2
																										+ 'px'
																							});
																		})
																.attr(
																		'src',
																		d.content);
														$('#data .image')
																.show();
														break;
													default:
														$('#data .default')
																.html(d.content)
																.show();
														break;
													}
												}
											});
						} else {
							$('#data .content').hide();
							$('#data .default').html(
									'Select a file from the tree.').show();
						}
					});

	$(".jstree-striped").removeClass("jstree-striped");

	$.ajax({
		url : "getCommitData",
		data:{
			'gitUrl' : $("#git_url").text()
		},
		dataType : "json",
		async : true,
		type : "GET",

		success : function(data) {

			for (var i = 0; i < data.length; i++) {
				commits.push(data[i]['commitId']);
			}

			activeIndex = commits.length - 1;

			$(".slider").slider({
				min : 0,
				max : commits.length - 1,
				value : activeIndex
			}).slider("pips", {
				rest : "pip",
				first : "pip",
				last : "pip"
			}).on(
					"slidechange",
					function(e, ui) {
						pastIndex = activeIndex;
						activeIndex = ui.value;
						$.ajax({
							url : "getJavaContent",
							type : "GET",
							data : {
								'gitUrl' : $("#git_url").text(),
								'commitId' : commits[activeIndex],
								'fileNodeId' : fileNodeId
							},

							success : function(result) {
								if (!result['exists']) {
									alert("文件不存在");
									activeIndex = pastIndex;
									$(".slider").slider("value", activeIndex)
											.slider("pips", "refresh");
								} else {

									$('#code').html(result['content']);
									$('pre code').each(function(i, block) {
										hljs.highlightBlock(block);
									});

								}
							}
						});

					});
		}
	});

	function showCommitSlider(fileNodeId) {
		$('#data .commitSlider').show();
	}

});
