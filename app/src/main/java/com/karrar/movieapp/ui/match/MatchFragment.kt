package com.karrar.movieapp.ui.match

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexboxLayout
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentMatchBinding
import com.karrar.movieapp.databinding.ItemMatchQuestionCardBinding
import com.karrar.movieapp.domain.enums.MediaType
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.main.MainActivity
import com.karrar.movieapp.ui.match.adapters.MatchCarouselAdapter
import com.karrar.movieapp.ui.match.adapters.MatchOptionsAdapter
import com.karrar.movieapp.ui.match.adapters.MatchResultsAdapter
import com.karrar.movieapp.utilities.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class MatchFragment : BaseFragment<FragmentMatchBinding>() {

    override val layoutIdFragment = R.layout.fragment_match
    override val viewModel: MatchViewModel by viewModels()

    private val optionsAdapter by lazy { MatchOptionsAdapter(::onOptionClick) }
    private val resultsAdapter by lazy { MatchResultsAdapter(::onMovieClick) }
    private val carouselAdapter by lazy { MatchCarouselAdapter(::onMovieClick) }

    private lateinit var backPressedCallback: OnBackPressedCallback
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private var currentCarouselPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        setupRecyclerViews()
        setupClickListeners()
        setupAppBar()
        setupBackPressedCallback()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        setupCarousel()
    }

    private fun setupCarousel() {
        try {
            val carousel = binding.matchResultsPage.moviesCarousel
            carousel.adapter = carouselAdapter

            carousel.offscreenPageLimit = 2
            carousel.clipToPadding = false
            carousel.clipChildren = false

            carousel.setPageTransformer { page: View, position: Float ->
                val pageOffset = abs(position).coerceIn(0f, 1f)

                val scale = 1f - (pageOffset * 0.15f)
                page.scaleX = scale
                page.scaleY = scale

                val alpha = 1f - (pageOffset * 0.7f)
                page.alpha = alpha

                page.translationZ = (1f - pageOffset) * 20f

                val offsetX = when {
                    position < 0 -> 40f * pageOffset
                    position > 0 -> -40f * pageOffset
                    else -> 0f
                }
                page.translationX = offsetX
            }

            carousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentCarouselPosition = position
                    updateDetailCard(position)
                    resetAutoScroll()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        binding.matchStartPage.startMatchingButton.setOnClickListener {
            (requireActivity() as? MainActivity)?.hideBottomNavigation()
            viewModel.onClickStartMatching()
        }

        binding.matchQuestionsPage.nextButton.setOnClickListener {
            viewModel.onClickNextQuestion()
        }

        try {
            binding.matchResultsPage.btnViewDetails.setOnClickListener {
                val matchResults = viewModel.uiState.value?.matchResults.orEmpty()
                if (matchResults.isNotEmpty() && currentCarouselPosition < matchResults.size) {
                    onMovieClick(matchResults[currentCarouselPosition].movieId)
                }
            }

            binding.matchResultsPage.btnSave.setOnClickListener {
                val matchResults = viewModel.uiState.value?.matchResults.orEmpty()
                if (matchResults.isNotEmpty() && currentCarouselPosition < matchResults.size) {
                    onSaveClick(matchResults[currentCarouselPosition].movieId)
                }
            }

            binding.matchResultsPage.btnPlayTrailer.setOnClickListener {
                val matchResults = viewModel.uiState.value?.matchResults.orEmpty()
                if (matchResults.isNotEmpty() && currentCarouselPosition < matchResults.size) {
                    onPlayTrailerClick(matchResults[currentCarouselPosition].movieId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAppBar() {
        binding.matchStartPage.appBar.btnBack.setOnClickListener {
            viewModel.onNavigateBack()
        }

        binding.matchQuestionsPage.appBar.btnBack.setOnClickListener {
            viewModel.onNavigateBack()
        }

        binding.matchResultsPage.appBar.btnBack.setOnClickListener {
            viewModel.onNavigateBack()
        }
    }

    private fun setupBackPressedCallback() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onNavigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun updateUI(state: MatchUiState) {
        if (state.shouldShowError) {
            binding.errorLayout.root.visibility = View.VISIBLE
            binding.matchContainer.visibility = View.GONE
            return
        }

        binding.errorLayout.root.visibility = View.GONE
        binding.matchContainer.visibility = View.VISIBLE

        when (state.currentPage) {
            MatchPages.START_PAGE -> {
                (requireActivity() as? MainActivity)?.showBottomNavigation()
                showStartPage()
            }

            MatchPages.QUESTIONS_PAGE -> {
                (requireActivity() as? MainActivity)?.hideBottomNavigation()
                showQuestionsPage(state)
            }

            MatchPages.RESULTS_PAGE -> {
                (requireActivity() as? MainActivity)?.hideBottomNavigation()
                showResultsPage(state)
            }
        }
    }

    private fun showStartPage() {
        binding.matchStartPage.root.visibility = View.VISIBLE
        binding.matchQuestionsPage.root.visibility = View.GONE
        binding.matchResultsPage.root.visibility = View.GONE

        updateAppBar(getString(R.string.discover_your_match), showBackButton = false)
    }

    private fun showQuestionsPage(state: MatchUiState) {
        binding.matchStartPage.root.visibility = View.GONE
        binding.matchQuestionsPage.root.visibility = View.VISIBLE
        binding.matchResultsPage.root.visibility = View.GONE

        val progress = (state.matchProgress * 100).toInt()
        binding.matchQuestionsPage.progressBar.progress = progress

        updateQuestionsLayout(state)

        binding.matchQuestionsPage.nextButton.isEnabled = state.isNextButtonActivated
        binding.matchQuestionsPage.nextButton.alpha =
            if (state.isNextButtonActivated) 1.0f else 0.5f

        val buttonText = if (state.currentQuestionType == QuestionType.TYPE) {
            getString(R.string.start_matching)
        } else {
            getString(R.string.next)
        }
        binding.matchQuestionsPage.nextButtonText.text = buttonText

        if (state.isLoadingRecommendations) {
            binding.matchQuestionsPage.nextButtonText.visibility = View.GONE
            binding.matchQuestionsPage.progressIndicator.visibility = View.VISIBLE
        } else {
            binding.matchQuestionsPage.nextButtonText.visibility = View.VISIBLE
            binding.matchQuestionsPage.progressIndicator.visibility = View.GONE
        }
        updateAppBar(getString(R.string.discover_your_match), showBackButton = true)
    }

    private fun updateQuestionsLayout(state: MatchUiState) {
        val currentOrder = state.currentQuestionType.ordinal

        updateMoodSection(
            state,
            currentOrder >= QuestionType.MOOD.ordinal,
            currentOrder == QuestionType.MOOD.ordinal
        )

        updateGenreSection(
            state,
            currentOrder >= QuestionType.GENRE.ordinal,
            currentOrder == QuestionType.GENRE.ordinal
        )

        updateTimeSection(
            state,
            currentOrder >= QuestionType.TIME.ordinal,
            currentOrder == QuestionType.TIME.ordinal
        )

        updateTypeSection(
            state,
            currentOrder >= QuestionType.TYPE.ordinal,
            currentOrder == QuestionType.TYPE.ordinal
        )
    }

    private fun updateMoodSection(state: MatchUiState, shouldShow: Boolean, isActive: Boolean) {
        binding.matchQuestionsPage.moodSection.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
        binding.matchQuestionsPage.moodSection.alpha = if (isActive) 1.0f else 0.3f

        if (shouldShow) {
            val questions = if (isActive) state.moodQuestions else state.selectedMoodQuestions
            setupMoodOptions(questions, isActive)
        }
    }

    private fun updateGenreSection(state: MatchUiState, shouldShow: Boolean, isActive: Boolean) {
        binding.matchQuestionsPage.genreSection.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
        binding.matchQuestionsPage.genreSection.alpha = if (isActive) 1.0f else 0.3f

        if (shouldShow) {
            val questions = if (isActive) state.genreQuestions else state.selectedGenres
            setupGenreOptions(questions, isActive)
        }
    }

    private fun updateTimeSection(state: MatchUiState, shouldShow: Boolean, isActive: Boolean) {
        binding.matchQuestionsPage.timeSection.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
        binding.matchQuestionsPage.timeSection.alpha = if (isActive) 1.0f else 0.3f

        if (shouldShow) {
            val questions = if (isActive) state.timeQuestions else state.selectedTimeQuestion
            setupTimeOptions(questions, isActive)
        }
    }

    private fun updateTypeSection(state: MatchUiState, shouldShow: Boolean, isActive: Boolean) {
        binding.matchQuestionsPage.typeSection.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
        val shouldDim = !isActive || state.isLoadingRecommendations
        binding.matchQuestionsPage.typeSection.alpha = if (shouldDim) 0.3f else 1.0f

        if (shouldShow) {
            val questions =
                if (isActive) state.movieTypeQuestions else state.selectedMovieTypeQuestion
            setupTypeOptions(questions, isActive)
        }
    }

    private fun setupMoodOptions(questions: List<QuestionUiState>, isActive: Boolean) {
        val containers = listOf(
            binding.matchQuestionsPage.moodOption1,
            binding.matchQuestionsPage.moodOption2,
            binding.matchQuestionsPage.moodOption3,
            binding.matchQuestionsPage.moodOption4
        )
        containers.forEach { it.removeAllViews() }
        containers.forEach { it.visibility = View.GONE }
        questions.forEachIndexed { index, question ->
            if (index < containers.size) {
                containers[index].visibility = View.VISIBLE
                val optionView = createQuestionCard(question, QuestionType.MOOD, isActive)
                containers[index].addView(optionView)
            }
        }
    }

    private fun setupGenreOptions(questions: List<QuestionUiState>, isActive: Boolean) {
        val container = binding.matchQuestionsPage.genreOptionsContainer
        container.removeAllViews()

        val margin = container.context.resources.getDimensionPixelSize(R.dimen.spacing_small)
        questions.forEach { question ->
            val optionView = createQuestionCard(question, QuestionType.GENRE, isActive)
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, margin, margin)
            }
            optionView.layoutParams = layoutParams
            container.addView(optionView)
        }
    }

    private fun setupTimeOptions(questions: List<QuestionUiState>, isActive: Boolean) {
        val container = binding.matchQuestionsPage.timeOptionsContainer
        container.removeAllViews()

        questions.forEach { question ->
            val optionView = createQuestionCard(question, QuestionType.TIME, isActive)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 14, 0, 10)
            }
            optionView.layoutParams = layoutParams
            container.addView(optionView)
        }
    }

    private fun setupTypeOptions(questions: List<QuestionUiState>, isActive: Boolean) {
        val containers = listOf(
            binding.matchQuestionsPage.typeOption1,
            binding.matchQuestionsPage.typeOption2,
            binding.matchQuestionsPage.typeOption3
        )

        containers.forEach { it.removeAllViews() }
        containers.forEach { it.visibility = View.GONE }
        questions.forEachIndexed { index, question ->
            if (index < containers.size) {
                containers[index].visibility = View.VISIBLE
                val optionView = createQuestionCard(question, QuestionType.TYPE, isActive)
                containers[index].addView(optionView)
            }
        }
    }

    private fun createQuestionCard(
        question: QuestionUiState,
        questionType: QuestionType,
        isActive: Boolean
    ): View {
        val cardView = layoutInflater.inflate(R.layout.item_match_question_card, null)
        val binding = ItemMatchQuestionCardBinding.bind(cardView)
        binding.question = question

        val hasIcon = question.iconResource != null
        val optionNameCenter =
            cardView.findViewById<TextView>(R.id.option_name_center)

        if (hasIcon) {
            binding.optionName.text = getString(question.name)
            binding.optionName.visibility = View.VISIBLE
            optionNameCenter.visibility = View.GONE

            question.description?.let { descId ->
                binding.optionDescription.text = getString(descId)
                binding.optionDescription.visibility = View.VISIBLE
            } ?: run {
                binding.optionDescription.visibility = View.GONE
            }

            question.iconResource?.let { iconRes ->
                binding.optionIcon.setImageResource(iconRes)
                binding.iconContainer.visibility = View.VISIBLE
            }
        } else {
            optionNameCenter.text = getString(question.name)
            optionNameCenter.visibility = View.VISIBLE
            binding.optionName.visibility = View.GONE
            binding.optionDescription.visibility = View.GONE
            binding.iconContainer.visibility = View.GONE
        }
        cardView.isSelected = question.isSelected

        if (isActive) {
            cardView.setOnClickListener {
                onOptionClick(question, questionType)
            }
        } else {
            cardView.setOnClickListener(null)
        }

        binding.executePendingBindings()
        return cardView
    }

    private fun showResultsPage(state: MatchUiState) {
        binding.matchStartPage.root.visibility = View.GONE
        binding.matchQuestionsPage.root.visibility = View.GONE
        binding.matchResultsPage.root.visibility = View.VISIBLE
        carouselAdapter.submitList(state.matchResults)

        if (state.matchResults.isNotEmpty()) {
            updateDetailCard(0)
            autoScrollHandler.postDelayed({
                resetAutoScroll()
            }, 3000)
        }
        updateAppBar(getString(R.string.match_list), showBackButton = true)
    }

    private fun updateAppBar(title: String, showBackButton: Boolean) {
        when (viewModel.uiState.value?.currentPage) {
            MatchPages.START_PAGE -> {
                binding.matchStartPage.appBar.btnBack.visibility =
                    View.GONE
                binding.matchStartPage.appBar.tvTitle.text = title
                binding.matchStartPage.appBar.tvTitle.visibility =
                    View.VISIBLE
            }

            MatchPages.QUESTIONS_PAGE -> {
                binding.matchQuestionsPage.appBar.btnBack.visibility =
                    if (showBackButton) View.VISIBLE else View.GONE
                binding.matchQuestionsPage.appBar.tvTitle.visibility = View.GONE
            }

            MatchPages.RESULTS_PAGE -> {
                binding.matchResultsPage.appBar.btnBack.visibility =
                    if (showBackButton) View.VISIBLE else View.GONE
                binding.matchResultsPage.appBar.tvTitle.text = title
                binding.matchResultsPage.appBar.tvTitle.visibility =
                    if (title.isNotEmpty()) View.VISIBLE else View.GONE
            }

            else -> {}
        }
    }

    private fun handleEvent(event: Event<MatchEvent?>) {
        var action: NavDirections?
        event.getContentIfNotHandled()?.let { matchEvent ->
            when (matchEvent) {
                is MatchEvent.OnMovieClick -> {
                    navigateToMovieDetails(matchEvent.id)
                }

                is MatchEvent.OnPlayTrailerClick -> {
                    action =
                        MatchFragmentDirections.actionMatchFragmentToYoutubePlayerActivity(
                            matchEvent.id, MediaType.MOVIE
                        )
                    findNavController().navigate(action)
                }

                is MatchEvent.OnSaveClick -> {
                    action =
                        MatchFragmentDirections.actionMatchFragmentToSavedMovieDialog(
                            matchEvent.id
                        )
                    findNavController().navigate(action)
                }

                MatchEvent.ShowLoginDialogEvent -> {
                    action =
                        MatchFragmentDirections.actionMatchFragmentToLogInDialog("")
                    findNavController().navigate(action)
                }
            }
            viewModel.resetEvent()
        }
    }

    private fun onOptionClick(question: QuestionUiState, questionType: QuestionType) {
        viewModel.onAnswerSelected(questionType, question)
    }

    private fun onMovieClick(movieId: Int) {
        viewModel.onMovieClick(movieId)
    }

    private fun navigateToMovieDetails(movieId: Int) {
        try {
            val bundle = Bundle().apply {
                putInt("movie_id", movieId)
            }
            findNavController().navigate(R.id.action_matchFragment_to_movieDetailFragment, bundle)
        } catch (e: Exception) {

        }
    }

    private fun updateDetailCard(position: Int) {
        try {
            val matchResults = viewModel.uiState.value?.matchResults.orEmpty()
            if (matchResults.isNotEmpty() && position < matchResults.size) {
                val movie = matchResults[position]

                binding.matchResultsPage.tvMovieTitle.text = movie.movieName
                binding.matchResultsPage.tvRating.text = movie.movieVoteAverage
                binding.matchResultsPage.tvReleaseDate.text = movie.movieReleasedDate
                binding.matchResultsPage.tvType.text = getString(R.string.movie)

                binding.matchResultsPage.tvGenres.text = movie.movieGenres
                binding.matchResultsPage.tvDuration.text = movie.movieDuration
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetAutoScroll() {
        try {
            autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }

            autoScrollRunnable = Runnable {
                val matchResults = viewModel.uiState.value?.matchResults.orEmpty()
                if (matchResults.isNotEmpty()) {
                    val nextPosition = (currentCarouselPosition + 1) % matchResults.size
                    binding.matchResultsPage.moviesCarousel.setCurrentItem(nextPosition, true)
                }
            }

            autoScrollRunnable?.let { runnable ->
                autoScrollHandler.postDelayed(runnable, 5000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onSaveClick(movieId: Int) {
        viewModel.onSaveClick(movieId)
    }

    private fun onPlayTrailerClick(movieId: Int) {
        viewModel.onPlayTrailerClick(movieId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        autoScrollRunnable = null

        (requireActivity() as? MainActivity)?.showBottomNavigation()
    }
}