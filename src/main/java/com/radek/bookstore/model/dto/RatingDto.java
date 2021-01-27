package com.radek.bookstore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RatingDto {

    @NotNull(message = "Rating cannot be null")
    @Min(value = 0, message = "Vote cannot be lower than 0")
    @Max(value = 5, message = "Vote cannot be greater than 5")
    private Integer vote;
}
